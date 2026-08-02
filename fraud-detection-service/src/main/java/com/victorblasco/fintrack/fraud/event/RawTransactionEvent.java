package com.victorblasco.fintrack.fraud.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Record que representa la transacción cruda recibida desde el topic Kafka {@code raw-transactions}.
 *
 * @param transactionId identificador único de la transacción
 * @param userId identificador del usuario propietario
 * @param accountNumber número de cuenta bancaria
 * @param amount importe monetario
 * @param currency código ISO de divisa (ej: EUR)
 * @param merchant nombre del comercio o entidad receptora
 * @param timestamp fecha y hora del movimiento
 */
public record RawTransactionEvent(
        UUID transactionId,
        UUID userId,
        String accountNumber,
        BigDecimal amount,
        String currency,
        String merchant,
        LocalDateTime timestamp
) {
}
