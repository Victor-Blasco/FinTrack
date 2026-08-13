package com.victorblasco.fintrack.ingest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento DTO transmitido al tópico de Kafka raw-transactions.
 *
 * @param transactionId identificador único de la transacción
 * @param userId identificador del usuario propietario de la cuenta
 * @param accountNumber número de cuenta IBAN
 * @param amount importe monetario
 * @param currency divisa (ej. EUR)
 * @param merchant comercio o beneficiario
 * @param timestamp marca temporal del evento
 * @param source canal de origen (ej. PSD2_WEBHOOK, CSV_IMPORT, MANUAL_ENTRY)
 * @param batchId identificador opcional del lote si proviene de una ingesta CSV
 *
 * @author Victor Blasco
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
