package com.victorblasco.fintrack.categorization.event;

import com.victorblasco.fintrack.categorization.domain.Verdict;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Evento consumido desde el topic Kafka {@code fraud-verdicts}.
 *
 * @param transactionId identificador único de la transacción
 * @param userId identificador del usuario
 * @param merchant nombre o descripción del comercio
 * @param verdict resultado de la evaluación de fraude (CLEAN o SUSPICIOUS)
 * @param reasons motivos de sospecha si los hubiera
 * @param evaluatedAt fecha y hora de la evaluación de fraude
 */
public record FraudVerdictEvent(
        UUID transactionId,
        UUID userId,
        String merchant,
        Verdict verdict,
        List<String> reasons,
        LocalDateTime evaluatedAt
) {
}
