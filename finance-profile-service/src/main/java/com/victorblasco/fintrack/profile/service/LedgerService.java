package com.victorblasco.fintrack.profile.service;

import com.victorblasco.fintrack.profile.dto.AccountSummaryResponse;
import com.victorblasco.fintrack.profile.dto.TransactionResponse;
import com.victorblasco.fintrack.profile.event.FraudVerdictEvent;
import com.victorblasco.fintrack.profile.event.RawTransactionEvent;
import com.victorblasco.fintrack.profile.event.TransactionCategorizedEvent;

import java.util.List;
import java.util.UUID;

/**
 * Interfaz del servicio de libro mayor contable para la consolidación de transacciones y saldos.
 */
public interface LedgerService {
    void processRawTransaction(RawTransactionEvent event);
    void applyFraudVerdict(FraudVerdictEvent event);
    void applyCategorization(TransactionCategorizedEvent event);
    AccountSummaryResponse getAccountSummary(UUID userId);
    List<TransactionResponse> getUserTransactions(UUID userId);
}
