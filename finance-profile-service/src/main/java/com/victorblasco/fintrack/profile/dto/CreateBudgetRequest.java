package com.victorblasco.fintrack.profile.dto;

import com.victorblasco.fintrack.profile.domain.model.Category;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Solicitud DTO para fijar un presupuesto mensual por categoría.
 */
public record CreateBudgetRequest(
        @NotNull(message = "La categoría es obligatoria")
        Category category,

        @NotNull(message = "El límite mensual es obligatorio")
        @Positive(message = "El límite mensual debe ser un valor positivo")
        BigDecimal monthlyLimit
) {}
