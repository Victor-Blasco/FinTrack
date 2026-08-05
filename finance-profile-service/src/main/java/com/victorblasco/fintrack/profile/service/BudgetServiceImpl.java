package com.victorblasco.fintrack.profile.service;

import com.victorblasco.fintrack.profile.domain.model.AlertLevel;
import com.victorblasco.fintrack.profile.domain.model.Budget;
import com.victorblasco.fintrack.profile.domain.model.Category;
import com.victorblasco.fintrack.profile.domain.repository.BudgetRepository;
import com.victorblasco.fintrack.profile.dto.BudgetResponse;
import com.victorblasco.fintrack.profile.dto.CreateBudgetRequest;
import com.victorblasco.fintrack.profile.event.BudgetAlertEvent;
import com.victorblasco.fintrack.profile.producer.BudgetAlertProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del servicio de presupuestos con incrementos atómicos y emisión de alertas.
 * <p>
 * Gestiona el límite mensual de gastos por categoría, acumula consumos atómicamente en PostgreSQL
 * mediante {@link BudgetRepository#incrementAccumulatedSpendAtomic} y notifica al tópico Kafka
 * {@code budget-alerts} cuando se supera el 80% (WARNING) o el 100% (CRITICAL).
 * </p>
 *
 * @author Victor Blasco
 */
@Service
public class BudgetServiceImpl implements BudgetService {

    private static final Logger log = LoggerFactory.getLogger(BudgetServiceImpl.class);

    private final BudgetRepository budgetRepository;
    private final BudgetAlertProducer budgetAlertProducer;

    /**
     * Construye el servicio inyectando el repositorio de presupuestos y el productor de alertas.
     *
     * @param budgetRepository repositorio JPA de presupuestos
     * @param budgetAlertProducer productor Kafka de alertas de presupuesto
     */
    public BudgetServiceImpl(BudgetRepository budgetRepository, BudgetAlertProducer budgetAlertProducer) {
        this.budgetRepository = budgetRepository;
        this.budgetAlertProducer = budgetAlertProducer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public BudgetResponse createOrUpdateBudget(UUID userId, CreateBudgetRequest request) {
        Optional<Budget> existingBudgetOpt = budgetRepository.findByUserIdAndCategory(userId, request.category());

        Budget budget;
        if (existingBudgetOpt.isPresent()) {
            budget = existingBudgetOpt.get();
            budget.setMonthlyLimit(request.monthlyLimit());
        } else {
            budget = new Budget(userId, request.category(), request.monthlyLimit(), BigDecimal.ZERO);
        }

        Budget saved = budgetRepository.save(budget);
        log.info("Presupuesto guardado/actualizado para usuario [{}] en categoría [{}] con límite [{}]",
                userId, request.category(), request.monthlyLimit());
        return mapToResponse(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getUserBudgets(UUID userId) {
        return budgetRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void applyExpenseToBudget(UUID userId, Category category, BigDecimal amount) {
        if (category == null || category == Category.UNASSIGNED || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        Optional<Budget> budgetOpt = budgetRepository.findByUserIdAndCategory(userId, category);
        if (budgetOpt.isEmpty()) {
            return;
        }

        Budget budget = budgetOpt.get();
        budgetRepository.incrementAccumulatedSpendAtomic(budget.getId(), amount);

        BigDecimal newAccumulated = budget.getAccumulatedSpend().add(amount);
        BigDecimal limit = budget.getMonthlyLimit();

        if (limit.compareTo(BigDecimal.ZERO) > 0) {
            double percentage = newAccumulated.divide(limit, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
            if (percentage >= 100.0) {
                budgetAlertProducer.sendAlert(new BudgetAlertEvent(
                        userId, category, AlertLevel.EXCEEDED_100_PERCENT, limit, newAccumulated, Instant.now()
                ));
            } else if (percentage >= 80.0) {
                budgetAlertProducer.sendAlert(new BudgetAlertEvent(
                        userId, category, AlertLevel.WARNING_80_PERCENT, limit, newAccumulated, Instant.now()
                ));
            }
        }
    }

    /**
     * Convierte la entidad {@link Budget} al DTO {@link BudgetResponse} calculando el porcentaje consumido.
     *
     * @param budget entidad de presupuesto
     * @return DTO de respuesta
     */
    private BudgetResponse mapToResponse(Budget budget) {
        double percentage = 0.0;
        if (budget.getMonthlyLimit() != null && budget.getMonthlyLimit().compareTo(BigDecimal.ZERO) > 0) {
            percentage = budget.getAccumulatedSpend()
                    .divide(budget.getMonthlyLimit(), 4, RoundingMode.HALF_UP)
                    .doubleValue() * 100.0;
        }
        return new BudgetResponse(
                budget.getId(),
                budget.getUserId(),
                budget.getCategory(),
                budget.getMonthlyLimit(),
                budget.getAccumulatedSpend(),
                percentage
        );
    }
}
