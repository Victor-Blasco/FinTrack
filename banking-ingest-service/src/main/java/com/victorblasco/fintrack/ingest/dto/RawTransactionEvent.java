package com.victorblasco.fintrack.ingest.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record RawTransactionEvent(
        String transactionId,
        String accountNumber,
        BigDecimal amount,
        String currency,
        String merchant,
        Instant timestamp,
        String source,
        String batchId
) {}
