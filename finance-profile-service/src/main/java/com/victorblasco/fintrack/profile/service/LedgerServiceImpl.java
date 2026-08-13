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
 * Implementación del libro mayor contable (Ledger) con ingesta de eventos de Kafka y actualización de estados.
 * <p>
 * Se encarga de procesar las transacciones crudas entrantes, aplicar los veredictos antifraude de FraudShield
 * de forma atómica, enriquecer las transacciones con su categoría asignada y proveer la consulta de saldos.
 * </p>
 *
 * @author Victor Blasco
 */
@Service
public class LedgerServiceImpl implements LedgerService {

    private static final Logger log = LoggerFactory.getLogger(LedgerServiceImpl.class);

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final BudgetService budgetService;

    /**
     * Construye el servicio inyectando los repositorios de transacciones y cuentas junto al servicio de presupuestos.
     *
     * @param transactionRepository repositorio JPA de transacciones
     * @param accountRepository repositorio JPA de cuentas bancarias
     * @param budgetService servicio de negocio de presupuestos
     */
    public LedgerServiceImpl(TransactionRepository transactionRepository, AccountRepository accountRepository, BudgetService budgetService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.budgetService = budgetService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void processRawTransaction(RawTransactionEvent event) {
        Optional<Transaction> existingTx = transactionRepository.findById(event.transactionId());
        if (existingTx.isPresent()) {
            Transaction tx = existingTx.get();
            log.info("La transacción [{}] ya fue registrada por veredicto con estado [{}]. Actualizando datos contables.",
                    event.transactionId(), tx.getStatus());
            if (event.amount() != null) {
                tx.setAmount(event.amount());
            }
            if (event.merchant() != null && !event.merchant().isBlank()) {
                tx.setMerchant(event.merchant());
            }
            transactionRepository.save(tx);
            return;
        }

        Account account = getOrCreateAccount(event.userId(), event.currency());

        LocalDateTime timestamp = LocalDateTime.ofInstant(event.timestamp(), ZoneOffset.UTC);
        Transaction tx = new Transaction(
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

        transactionRepository.save(tx);
        log.info("Transacción cruda [{}] registrada con estado PENDING", tx.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void applyFraudVerdict(FraudVerdictEvent event) {
        Optional<Transaction> txOpt = transactionRepository.findById(event.transactionId());
        Transaction tx;

        if (txOpt.isPresent()) {
            tx = txOpt.get();
        } else {
            log.warn("Transacción [{}] no encontrada al recibir veredicto. Creando entidad con estado definitivo [{}]",
                    event.transactionId(), event.verdict());
            Account account = getOrCreateAccount(event.userId(), "EUR");

            LocalDateTime timestamp = event.evaluatedAt() != null ? event.evaluatedAt() : LocalDateTime.now();
            BigDecimal amount = event.amount() != null ? event.amount() : BigDecimal.ZERO;
            String merchant = event.merchant() != null ? event.merchant() : "Desconocido";

            tx = new Transaction(
                    event.transactionId(),
                    account,
                    event.userId(),
                    amount,
                    account.getCurrency(),
                    merchant,
                    Status.PENDING,
                    Category.UNASSIGNED,
                    timestamp
            );
        }

        if ("CLEAN".equalsIgnoreCase(event.verdict())) {
            tx.setStatus(Status.PROCESSED);
            if (tx.getAmount() != null && tx.getAmount().compareTo(BigDecimal.ZERO) != 0) {
                accountRepository.updateBalanceAtomic(tx.getAccount().getId(), tx.getAmount());
                budgetService.applyExpenseToBudget(tx.getUserId(), tx.getCategory(), tx.getAmount());
            }
            log.info("Veredicto CLEAN aplicado a transacción [{}]", tx.getId());
        } else {
            tx.setStatus(Status.QUARANTINED);
            log.warn("Veredicto SUSPICIOUS (CUARENTENA) aplicado a transacción [{}]", tx.getId());
        }
        transactionRepository.save(tx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void applyCategorization(TransactionCategorizedEvent event) {
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

        if (tx.getStatus() == Status.PROCESSED) {
            budgetService.applyExpenseToBudget(tx.getUserId(), newCategory, tx.getAmount());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public AccountSummaryResponse getAccountSummary(UUID userId) {
        Account account = accountRepository.findFirstByUserId(userId)
                .orElseGet(() -> new Account(userId, "ES00-0000-0000-0000", BigDecimal.ZERO, "EUR"));

        return new AccountSummaryResponse(
                account.getId(),
                account.getUserId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency()
        );
    }

    private Account getOrCreateAccount(UUID userId, String currency) {
        Optional<Account> accountOpt = accountRepository.findFirstByUserId(userId);
        if (accountOpt.isPresent()) {
            return accountOpt.get();
        }
        try {
            return accountRepository.save(new Account(userId, "ES00-0000-0000-0000", BigDecimal.ZERO, currency != null ? currency : "EUR"));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return accountRepository.findFirstByUserId(userId)
                    .orElseThrow(() -> new IllegalStateException("No se pudo obtener ni crear la cuenta para el usuario: " + userId, e));
        }
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public TransactionResponse updateTransactionStatus(UUID userId, UUID transactionId, String status) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada con ID: " + transactionId));

        if (!tx.getUserId().equals(userId)) {
            throw new IllegalArgumentException("La transacción no pertenece al usuario especificado");
        }

        Status newStatus = Status.valueOf(status.toUpperCase());
        tx.setStatus(newStatus);
        Transaction saved = transactionRepository.save(tx);
        log.info("Estado de seguridad de la transacción [{}] actualizado a [{}]", transactionId, newStatus);

        return new TransactionResponse(
                saved.getId(),
                saved.getAmount(),
                saved.getCurrency(),
                saved.getMerchant(),
                saved.getStatus(),
                saved.getCategory(),
                saved.getTimestamp()
        );
    }
}
