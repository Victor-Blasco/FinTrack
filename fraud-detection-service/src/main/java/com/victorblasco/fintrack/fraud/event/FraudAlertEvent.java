package com.victorblasco.fintrack.fraud.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Record que representa una alerta de seguridad prioritaria emitida al topic Kafka {@code fraud-alerts}.
 *
 * @param transactionId identificador de la transacción sospechosa
 * @param userId identificador del usuario
 * @param amount importe monetario
 * @param merchant comercio o entidad origen
 * @param reasons lista de razones de riesgo activadas
 * @param triggeredAt marca de tiempo en la que se generó la alerta
 */
public record FraudAlertEvent(
        UUID transactionId,
        UUID userId,
        BigDecimal amount,
        String merchant,
        List<String> reasons,
        LocalDateTime triggeredAt
) {
}
