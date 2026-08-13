package com.victorblasco.fintrack.ingest.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Registro DTO de respuesta devuelto al completar la ingesta manual de un gasto.
 *
 * @param transactionId identificador único asignado a la transacción
 * @param status estado inicial de la ingesta (ej. PENDING)
 * @param merchant comercio registrado
 * @param amount importe del gasto
 * @param createdAt fecha y hora de creación
 *
 * @author Victor Blasco
 */
public record TransactionManualResponse(
        String transactionId,
        String status,
        String merchant,
        BigDecimal amount,
        Instant createdAt
) {}
