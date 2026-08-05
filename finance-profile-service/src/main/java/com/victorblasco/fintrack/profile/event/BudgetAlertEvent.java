package com.victorblasco.fintrack.profile.event;

import com.victorblasco.fintrack.profile.domain.model.AlertLevel;
import com.victorblasco.fintrack.profile.domain.model.Category;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento de alerta de presupuesto saliente publicado al tópico budget-alerts.
 */
public record BudgetAlertEvent(
        UUID userId,
        Category category,
        AlertLevel alertLevel,
        BigDecimal limit,
        BigDecimal accumulated,
        Instant timestamp
) {}
