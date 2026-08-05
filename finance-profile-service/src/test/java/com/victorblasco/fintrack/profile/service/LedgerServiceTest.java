package com.victorblasco.fintrack.profile.service;

import com.victorblasco.fintrack.profile.domain.model.*;
import com.victorblasco.fintrack.profile.domain.repository.AccountRepository;
import com.victorblasco.fintrack.profile.domain.repository.TransactionRepository;
import com.victorblasco.fintrack.profile.event.FraudVerdictEvent;
import com.victorblasco.fintrack.profile.event.RawTransactionEvent;
import com.victorblasco.fintrack.profile.event.TransactionCategorizedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BudgetService budgetService;

    private LedgerServiceImpl ledgerService;

    @BeforeEach
    void setUp() {
        ledgerService = new LedgerServiceImpl(accountRepository, transactionRepository, budgetService);
    }

    @Test
    @DisplayName("DADO un evento raw-transaction CUANDO se procesa ENTONCES registra la transacción con estado PENDING y categoría UNASSIGNED")
    void shouldProcessRawTransactionAsPending() {
        UUID txId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        RawTransactionEvent event = new RawTransactionEvent(
                txId, userId, "ES1234", new BigDecimal("150.00"), "EUR", "MERCADONA", Instant.now(), "CSV", "batch-1"
        );

        when(accountRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        ledgerService.processRawTransaction(event);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction savedTx = captor.getValue();
        assertThat(savedTx.getId()).isEqualTo(txId);
        assertThat(savedTx.getStatus()).isEqualTo(Status.PENDING);
        assertThat(savedTx.getCategory()).isEqualTo(Category.UNASSIGNED);
        assertThat(savedTx.getAmount()).isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("DADO un veredicto CLEAN CUANDO se aplica ENTONCES actualiza estado a PROCESSED y modifica el saldo atómicamente")
    void shouldApplyCleanVerdictAndBalanceAtomic() {
        UUID txId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Account account = new Account(userId, "ES1234", new BigDecimal("500.00"), "EUR");
        account.setId(UUID.randomUUID());

        Transaction tx = new Transaction(
                txId, account, userId, new BigDecimal("100.00"), "EUR", "ZARA", Status.PENDING, Category.UNASSIGNED, LocalDateTime.now()
        );

        when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));

        FraudVerdictEvent event = new FraudVerdictEvent(txId, userId, "ZARA", "CLEAN", List.of(), LocalDateTime.now());
        ledgerService.applyFraudVerdict(event);

        assertThat(tx.getStatus()).isEqualTo(Status.PROCESSED);
        verify(transactionRepository).save(tx);
        verify(accountRepository).updateBalanceAtomic(account.getId(), new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("DADO un veredicto SUSPICIOUS CUANDO se aplica ENTONCES pone la transacción en QUARANTINED sin tocar saldos")
    void shouldQuarantineSuspiciousTransaction() {
        UUID txId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Account account = new Account(userId, "ES1234", new BigDecimal("500.00"), "EUR");
        account.setId(UUID.randomUUID());

        Transaction tx = new Transaction(
                txId, account, userId, new BigDecimal("2500.00"), "EUR", "CASINO", Status.PENDING, Category.UNASSIGNED, LocalDateTime.now()
        );

        when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));

        FraudVerdictEvent event = new FraudVerdictEvent(txId, userId, "CASINO", "SUSPICIOUS", List.of("HIGH_AMOUNT"), LocalDateTime.now());
        ledgerService.applyFraudVerdict(event);

        assertThat(tx.getStatus()).isEqualTo(Status.QUARANTINED);
        verify(transactionRepository).save(tx);
        verify(accountRepository, never()).updateBalanceAtomic(any(), any());
    }

    @Test
    @DisplayName("DADO un evento categorizado CUANDO se aplica ENTONCES actualiza la categoría de la transacción")
    void shouldApplyCategorizationToTransaction() {
        UUID txId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Account account = new Account(userId, "ES1234", new BigDecimal("500.00"), "EUR");

        Transaction tx = new Transaction(
                txId, account, userId, new BigDecimal("50.00"), "EUR", "MERCADONA", Status.PROCESSED, Category.UNASSIGNED, LocalDateTime.now()
        );

        when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));

        TransactionCategorizedEvent event = new TransactionCategorizedEvent(txId, "ALIMENTACION", LocalDateTime.now());
        ledgerService.applyCategorization(event);

        assertThat(tx.getCategory()).isEqualTo(Category.ALIMENTACION);
        verify(transactionRepository).save(tx);
        verify(budgetService).applyExpenseToBudget(userId, Category.ALIMENTACION, new BigDecimal("50.00"));
    }
}
