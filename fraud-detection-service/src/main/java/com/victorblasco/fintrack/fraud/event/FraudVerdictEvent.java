package com.victorblasco.fintrack.fraud.event;

import com.victorblasco.fintrack.fraud.domain.Verdict;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Record que representa el veredicto final emitido al topic Kafka {@code fraud-verdicts}.
 *
 * @param transactionId identificador de la transacción evaluada
 * @param userId identificador del usuario
 * @param verdict veredicto resultante (CLEAN o SUSPICIOUS)
 * @param reasons lista de razones de riesgo detectadas
 * @param evaluatedAt marca de tiempo de la evaluación
 */
public record FraudVerdictEvent(
        UUID transactionId,
        UUID userId,
        Verdict verdict,
        List<String> reasons,
        LocalDateTime evaluatedAt
) {
}
