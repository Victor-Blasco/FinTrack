package com.victorblasco.fintrack.profile.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resumen DTO con información de saldo y cuenta bancaria del usuario.
 */
public record AccountSummaryResponse(
        UUID accountId,
        UUID userId,
        String accountNumber,
        BigDecimal balance,
        String currency
) {}
