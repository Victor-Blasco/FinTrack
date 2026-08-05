package com.victorblasco.fintrack.profile.domain.model;

/**
 * Estado del libro mayor para una transacción bancaria.
 */
public enum Status {
    PENDING,
    PROCESSED,
    QUARANTINED
}
