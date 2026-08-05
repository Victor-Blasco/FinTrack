package com.victorblasco.fintrack.profile.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidad JPA que representa un presupuesto mensual fijado por el usuario para una categoría.
 */
@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    @Column(name = "monthly_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(name = "accumulated_spend", nullable = false, precision = 15, scale = 2)
    private BigDecimal accumulatedSpend;

    public Budget() {}

    public Budget(UUID userId, Category category, BigDecimal monthlyLimit, BigDecimal accumulatedSpend) {
        this.userId = userId;
        this.category = category;
        this.monthlyLimit = monthlyLimit;
        this.accumulatedSpend = accumulatedSpend != null ? accumulatedSpend : BigDecimal.ZERO;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public BigDecimal getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(BigDecimal monthlyLimit) { this.monthlyLimit = monthlyLimit; }
    public BigDecimal getAccumulatedSpend() { return accumulatedSpend; }
    public void setAccumulatedSpend(BigDecimal accumulatedSpend) { this.accumulatedSpend = accumulatedSpend; }
}
