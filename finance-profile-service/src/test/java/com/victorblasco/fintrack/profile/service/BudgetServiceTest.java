package com.victorblasco.fintrack.profile.service;

import com.victorblasco.fintrack.profile.domain.model.AlertLevel;
import com.victorblasco.fintrack.profile.domain.model.Budget;
import com.victorblasco.fintrack.profile.domain.model.Category;
import com.victorblasco.fintrack.profile.domain.repository.BudgetRepository;
import com.victorblasco.fintrack.profile.dto.BudgetResponse;
import com.victorblasco.fintrack.profile.dto.CreateBudgetRequest;
import com.victorblasco.fintrack.profile.event.BudgetAlertEvent;
import com.victorblasco.fintrack.profile.producer.BudgetAlertProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private BudgetAlertProducer budgetAlertProducer;

    private BudgetServiceImpl budgetService;

    @BeforeEach
    void setUp() {
        budgetService = new BudgetServiceImpl(budgetRepository, budgetAlertProducer);
    }

    @Test
    @DisplayName("DADO un presupuesto nuevo CUANDO se guarda ENTONCES calcula el porcentaje de consumo e inserta en DB")
    void shouldCreateNewBudget() {
        UUID userId = UUID.randomUUID();
        CreateBudgetRequest request = new CreateBudgetRequest(Category.ALIMENTACION, new BigDecimal("500.00"));

        when(budgetRepository.findByUserIdAndCategory(userId, Category.ALIMENTACION)).thenReturn(Optional.empty());
        when(budgetRepository.save(any(Budget.class))).thenAnswer(i -> {
            Budget b = i.getArgument(0);
            b.setId(UUID.randomUUID());
            return b;
        });

        BudgetResponse response = budgetService.createOrUpdateBudget(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.category()).isEqualTo(Category.ALIMENTACION);
        assertThat(response.monthlyLimit()).isEqualByComparingTo("500.00");
        assertThat(response.accumulatedSpend()).isEqualByComparingTo("0.00");
        assertThat(response.percentageUsed()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("DADO un gasto que alcanza el 80% del límite CUANDO se aplica ENTONCES emite una alerta WARNING_80_PERCENT")
    void shouldEmitWarningAlertWhenReaching80Percent() {
        UUID userId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        Budget budget = new Budget(userId, Category.ALIMENTACION, new BigDecimal("100.00"), new BigDecimal("70.00"));
        budget.setId(budgetId);

        when(budgetRepository.findByUserIdAndCategory(userId, Category.ALIMENTACION)).thenReturn(Optional.of(budget));

        budgetService.applyExpenseToBudget(userId, Category.ALIMENTACION, new BigDecimal("15.00"));

        verify(budgetRepository).incrementAccumulatedSpendAtomic(budgetId, new BigDecimal("15.00"));

        ArgumentCaptor<BudgetAlertEvent> captor = ArgumentCaptor.forClass(BudgetAlertEvent.class);
        verify(budgetAlertProducer).sendAlert(captor.capture());

        BudgetAlertEvent alert = captor.getValue();
        assertThat(alert.alertLevel()).isEqualTo(AlertLevel.WARNING_80_PERCENT);
        assertThat(alert.accumulated()).isEqualByComparingTo("85.00");
    }

    @Test
    @DisplayName("DADO un gasto que supera el 100% del límite CUANDO se aplica ENTONCES emite una alerta EXCEEDED_100_PERCENT")
    void shouldEmitCriticalAlertWhenExceeding100Percent() {
        UUID userId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        Budget budget = new Budget(userId, Category.ALIMENTACION, new BigDecimal("100.00"), new BigDecimal("90.00"));
        budget.setId(budgetId);

        when(budgetRepository.findByUserIdAndCategory(userId, Category.ALIMENTACION)).thenReturn(Optional.of(budget));

        budgetService.applyExpenseToBudget(userId, Category.ALIMENTACION, new BigDecimal("20.00"));

        ArgumentCaptor<BudgetAlertEvent> captor = ArgumentCaptor.forClass(BudgetAlertEvent.class);
        verify(budgetAlertProducer).sendAlert(captor.capture());

        BudgetAlertEvent alert = captor.getValue();
        assertThat(alert.alertLevel()).isEqualTo(AlertLevel.EXCEEDED_100_PERCENT);
        assertThat(alert.accumulated()).isEqualByComparingTo("110.00");
    }
}
