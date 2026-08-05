package com.victorblasco.fintrack.profile.service;

import com.victorblasco.fintrack.profile.domain.model.Category;
import com.victorblasco.fintrack.profile.dto.BudgetResponse;
import com.victorblasco.fintrack.profile.dto.CreateBudgetRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Interfaz del servicio de negocio para la gestión de presupuestos mensuales y alertas de consumo.
 * <p>
 * Define operaciones para crear/actualizar límites por categoría, consultar consumos
 * y aplicar gastos de forma atómica emitiendo alertas a Kafka cuando se alcanza el 80% o 100%.
 * </p>
 *
 * @author Victor Blasco
 */
public interface BudgetService {

    /**
     * Crea un nuevo presupuesto o actualiza el límite mensual existente para una categoría.
     *
     * @param userId identificador del usuario
     * @param request DTO {@link CreateBudgetRequest} con la categoría y el límite mensual
     * @return DTO {@link BudgetResponse} con la información del presupuesto actualizado
     */
    BudgetResponse createOrUpdateBudget(UUID userId, CreateBudgetRequest request);

    /**
     * Obtiene la lista de presupuestos configurados por el usuario.
     *
     * @param userId identificador del usuario
     * @return lista de DTOs {@link BudgetResponse} con consumos y porcentajes
     */
    List<BudgetResponse> getUserBudgets(UUID userId);

    /**
     * Incrementa atómicamente el gasto acumulado de un presupuesto y evalúa umbrales de alerta.
     *
     * @param userId identificador del usuario
     * @param category categoría de gasto
     * @param amount importe del gasto a imputar
     */
    void applyExpenseToBudget(UUID userId, Category category, BigDecimal amount);
}
