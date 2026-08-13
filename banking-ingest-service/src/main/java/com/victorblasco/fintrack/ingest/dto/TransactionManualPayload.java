package com.victorblasco.fintrack.ingest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Registro DTO para la carga útil de entrada en ingesta manual de gastos.
 *
 * @param merchant nombre del comercio o concepto del gasto
 * @param amount importe del gasto en valor positivo
 * @param accountNumber número de cuenta IBAN opcional
 * @param currency divisa de la transacción (ej. EUR)
 *
 * @author Victor Blasco
 */
public record TransactionManualPayload(
        @NotBlank(message = "El nombre del comercio es obligatorio")
        String merchant,

        @NotNull(message = "El importe es obligatorio")
        @Positive(message = "El importe debe ser un valor positivo")
        BigDecimal amount,

        String accountNumber,

        String currency
) {}
