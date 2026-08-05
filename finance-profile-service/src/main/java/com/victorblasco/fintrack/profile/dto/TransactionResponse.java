package com.victorblasco.fintrack.profile.dto;

import com.victorblasco.fintrack.profile.domain.model.Category;
import com.victorblasco.fintrack.profile.domain.model.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resumen DTO para representar movimientos contables en las respuestas de la API REST.
 */
public record TransactionResponse(
        UUID transactionId,
        BigDecimal amount,
        String currency,
        String merchant,
        Status status,
        Category category,
        LocalDateTime timestamp
) {}
