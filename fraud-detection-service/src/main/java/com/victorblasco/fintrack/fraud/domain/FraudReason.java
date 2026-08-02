package com.victorblasco.fintrack.fraud.domain;

/**
 * Razones y factores de riesgo identificados durante la evaluación de seguridad.
 */
public enum FraudReason {
    /**
     * Anomalía de importe muy elevado (> 2.000,00 EUR).
     */
    HIGH_AMOUNT_ANOMALY,

    /**
     * Ráfaga de más de 3 transacciones en una ventana de 60 segundos para el mismo usuario.
     */
    HIGH_FREQUENCY_RULE,

    /**
     * Comercio catalogado como categoría o entidad de alto riesgo.
     */
    HIGH_RISK_MERCHANT,

    /**
     * Operación realizada en horario nocturno anómalo (01:00 AM - 05:59 AM).
     */
    OFF_HOURS_ACTIVITY
}
