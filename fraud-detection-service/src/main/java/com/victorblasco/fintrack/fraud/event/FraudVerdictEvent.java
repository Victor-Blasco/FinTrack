package com.victorblasco.fintrack.fraud.event;

import com.victorblasco.fintrack.fraud.domain.Verdict;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Record que representa el veredicto final emitido al topic Kafka {@code fraud-verdicts}.
 *
 * @param transactionId identificador de la transacción evaluada
 * @param userId identificador del usuario
 * @param merchant nombre/identificador del comercio
 * @param verdict veredicto resultante (CLEAN o SUSPICIOUS)
 * @param reasons lista de razones de riesgo detectadas
 * @param evaluatedAt marca de tiempo de la evaluación
 */
public record FraudVerdictEvent(
        UUID transactionId,
        UUID userId,
        BigDecimal amount,
        String merchant,
        Verdict verdict,
        int riskScore,
        List<String> reasons,
        LocalDateTime evaluatedAt
) {
}
