package com.victorblasco.fintrack.fraud.domain;

import java.util.List;

/**
 * Record que encapsula el resultado completo del motor de puntuación de riesgo.
 *
 * @param verdict veredicto final (CLEAN o SUSPICIOUS)
 * @param riskScore puntuación acumulada de riesgo (0 a 100)
 * @param reasons lista de razones o reglas activadas durante la evaluación
 */
public record FraudEvaluationResult(
        Verdict verdict,
        int riskScore,
        List<FraudReason> reasons
) {
}
