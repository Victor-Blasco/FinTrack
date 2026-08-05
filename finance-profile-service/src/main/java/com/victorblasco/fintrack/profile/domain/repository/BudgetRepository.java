package com.victorblasco.fintrack.profile.domain.repository;

import com.victorblasco.fintrack.profile.domain.model.Budget;
import com.victorblasco.fintrack.profile.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para la entidad {@link Budget} con consulta de incremento atómico de gasto acumulado.
 */
public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    Optional<Budget> findByUserIdAndCategory(UUID userId, Category category);

    List<Budget> findByUserId(UUID userId);

    @Modifying
    @Query("UPDATE Budget b SET b.accumulatedSpend = b.accumulatedSpend + :amount WHERE b.id = :budgetId")
    int incrementAccumulatedSpendAtomic(@Param("budgetId") UUID budgetId, @Param("amount") BigDecimal amount);
}
