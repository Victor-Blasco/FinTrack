package com.victorblasco.fintrack.fraud.service;

import com.victorblasco.fintrack.fraud.domain.FraudEvaluationResult;
import com.victorblasco.fintrack.fraud.event.RawTransactionEvent;

/**
 * Interfaz del motor de reglas deterministas de seguridad antifraude.
 */
public interface FraudRuleEngine {

    /**
     * Evalúa una transacción bancaria entrante acumulando una puntuación de riesgo (0 a 100).
     *
     * @param event transacción bancaria a evaluar {@link RawTransactionEvent}
     * @return resultado de evaluación con veredicto, puntuación y razones {@link FraudEvaluationResult}
     */
    FraudEvaluationResult evaluate(RawTransactionEvent event);
}
