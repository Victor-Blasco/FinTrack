package com.victorblasco.fintrack.profile.service;

import com.victorblasco.fintrack.profile.domain.model.Category;
import com.victorblasco.fintrack.profile.dto.BudgetResponse;
import com.victorblasco.fintrack.profile.dto.CreateBudgetRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Interfaz de servicio de negocio para la gestión de presupuestos y alertas de consumo.
 */
public interface BudgetService {
    BudgetResponse createOrUpdateBudget(UUID userId, CreateBudgetRequest request);
    List<BudgetResponse> getUserBudgets(UUID userId);
    void applyExpenseToBudget(UUID userId, Category category, BigDecimal amount);
}
