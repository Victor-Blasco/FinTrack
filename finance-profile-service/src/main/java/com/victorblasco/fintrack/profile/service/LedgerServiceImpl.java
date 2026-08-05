package com.victorblasco.fintrack.profile.service;

import com.victorblasco.fintrack.profile.domain.model.*;
import com.victorblasco.fintrack.profile.domain.repository.AccountRepository;
import com.victorblasco.fintrack.profile.domain.repository.TransactionRepository;
import com.victorblasco.fintrack.profile.dto.AccountSummaryResponse;
import com.victorblasco.fintrack.profile.dto.TransactionResponse;
import com.victorblasco.fintrack.profile.event.FraudVerdictEvent;
import com.victorblasco.fintrack.profile.event.RawTransactionEvent;
import com.victorblasco.fintrack.profile.event.TransactionCategorizedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del servicio de libro mayor con soporte de consolidación de eventos Kafka y saldos atómicos.
 */
@Service
public class LedgerServiceImpl implements LedgerService {

    private static final Logger log = LoggerFactory.getLogger(LedgerServiceImpl.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetService budgetService;

    public LedgerServiceImpl(AccountRepository accountRepository,
                             TransactionRepository transactionRepository,
                             BudgetService budgetService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.budgetService = budgetService;
    }

    @Override
    @Transactional
    public void processRawTransaction(RawTransactionEvent event) {
        if (event == null || event.transactionId() == null || event.userId() == null) {
            log.error("Evento raw-transaction nulo o malformado recibido en LedgerService");
            return;
        }

        Account account = accountRepository.findByUserId(event.userId())
                .orElseGet(() -> {
                    String accountNumber = event.accountNumber() != null ? event.accountNumber() : "ES00-0000-0000-0000";
                    Account newAccount = new Account(event.userId(), accountNumber, BigDecimal.ZERO, event.currency());
                    return accountRepository.save(newAccount);
                });

        LocalDateTime timestamp = event.timestamp() != null
                ? LocalDateTime.ofInstant(event.timestamp(), ZoneOffset.UTC)
                : LocalDateTime.now();

        Transaction transaction = new Transaction(
                event.transactionId(),
                account,
                event.userId(),
                event.amount(),
                event.currency(),
                event.merchant(),
                Status.PENDING,
                Category.UNASSIGNED,
                timestamp
        );

        transactionRepository.save(transaction);
        log.info("Transacción registrada con estado PENDING para transactionId [{}]", event.transactionId());
    }

    @Override
    @Transactional
    public void applyFraudVerdict(FraudVerdictEvent event) {
        if (event == null || event.transactionId() == null) {
            return;
        }

        Optional<Transaction> txOpt = transactionRepository.findById(event.transactionId());
        if (txOpt.isEmpty()) {
            log.warn("Transacción [{}] no encontrada para aplicar veredicto de fraude", event.transactionId());
            return;
        }

        Transaction tx = txOpt.get();
        if ("CLEAN".equalsIgnoreCase(event.verdict())) {
            tx.setStatus(Status.PROCESSED);
            transactionRepository.save(tx);

            // Actualización atómica del saldo de la cuenta
            accountRepository.updateBalanceAtomic(tx.getAccount().getId(), tx.getAmount());
            log.info("Transacción [{}] consolidada como PROCESSED. Saldo actualizado atómicamente.", tx.getId());

            // Si ya tiene categoría asignada, aplicar al presupuesto
            if (tx.getCategory() != null && tx.getCategory() != Category.UNASSIGNED) {
                budgetService.applyExpenseToBudget(tx.getUserId(), tx.getCategory(), tx.getAmount());
            }
        } else if ("SUSPICIOUS".equalsIgnoreCase(event.verdict())) {
            tx.setStatus(Status.QUARANTINED);
            transactionRepository.save(tx);
            log.warn("Transacción [{}] en cuarentena (QUARANTINED) por sospecha de fraude", tx.getId());
        }
    }

    @Override
    @Transactional
    public void applyCategorization(TransactionCategorizedEvent event) {
        if (event == null || event.transactionId() == null || event.category() == null) {
            return;
        }

        Optional<Transaction> txOpt = transactionRepository.findById(event.transactionId());
        if (txOpt.isEmpty()) {
            log.warn("Transacción [{}] no encontrada para aplicar categoría", event.transactionId());
            return;
        }

        Transaction tx = txOpt.get();
        Category newCategory;
        try {
            newCategory = Category.valueOf(event.category());
        } catch (IllegalArgumentException e) {
            newCategory = Category.OTROS;
        }

        tx.setCategory(newCategory);
        transactionRepository.save(tx);
        log.info("Categoría [{}] asignada a la transacción [{}]", newCategory, tx.getId());

        // Si la transacción ya estaba PROCESSED, aplicar al presupuesto de la nueva categoría
        if (tx.getStatus() == Status.PROCESSED) {
            budgetService.applyExpenseToBudget(tx.getUserId(), newCategory, tx.getAmount());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AccountSummaryResponse getAccountSummary(UUID userId) {
        Account account = accountRepository.findByUserId(userId)
                .orElseGet(() -> new Account(userId, "ES00-0000-0000-0000", BigDecimal.ZERO, "EUR"));

        return new AccountSummaryResponse(
                account.getId(),
                account.getUserId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getUserTransactions(UUID userId) {
        return transactionRepository.findByUserIdOrderByTimestampDesc(userId).stream()
                .map(tx -> new TransactionResponse(
                        tx.getId(),
                        tx.getAmount(),
                        tx.getCurrency(),
                        tx.getMerchant(),
                        tx.getStatus(),
                        tx.getCategory(),
                        tx.getTimestamp()
                ))
                .toList();
    }
}
