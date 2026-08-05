package com.victorblasco.fintrack.profile.dto;

import com.victorblasco.fintrack.profile.domain.model.Category;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resumen DTO con información de presupuestos y porcentaje de consumo.
 */
public record BudgetResponse(
        UUID budgetId,
        UUID userId,
        Category category,
        BigDecimal monthlyLimit,
        BigDecimal accumulatedSpend,
        double percentageUsed
) {}
