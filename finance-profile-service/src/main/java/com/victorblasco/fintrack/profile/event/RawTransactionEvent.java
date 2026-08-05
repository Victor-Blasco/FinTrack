package com.victorblasco.fintrack.profile.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento de transacción cruda entrante desde el tópico raw-transactions.
 */
public record RawTransactionEvent(
        UUID transactionId,
        UUID userId,
        String accountNumber,
        BigDecimal amount,
        String currency,
        String merchant,
        Instant timestamp,
        String source,
        String batchId
) {}
