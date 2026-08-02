package com.victorblasco.fintrack.fraud.domain;

/**
 * Enumeración que representa el veredicto de evaluación de seguridad de una transacción.
 */
public enum Verdict {
    /**
     * Transacción limpia sin indicios de fraude ni anomalías ponderadas.
     */
    CLEAN,

    /**
     * Transacción sospechosa bloqueada preventivamente o enviada a cuarentena.
     */
    SUSPICIOUS
}
