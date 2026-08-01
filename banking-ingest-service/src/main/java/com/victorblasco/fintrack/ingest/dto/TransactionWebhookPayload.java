package com.victorblasco.fintrack.ingest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Registro DTO que representa la carga útil de entrada para webhooks de transacciones PSD2.
 *
 * @param transactionId identificador único de la transacción en la entidad bancaria
 * @param accountNumber número de cuenta IBAN de origen
 * @param amount importe monetario de la transacción
 * @param currency divisa de la transacción (ej. EUR)
 * @param merchant comercio o beneficiario del pago
 * @param timestamp fecha y hora en la que se efectuó la transacción
 *
 * @author Victor Blasco
 */
public record TransactionWebhookPayload(
        @NotBlank(message = "El transactionId es obligatorio")
        String transactionId,

        @NotBlank(message = "El accountNumber es obligatorio")
        String accountNumber,

        @NotNull(message = "El amount es obligatorio")
        @Positive(message = "El importe debe ser un valor positivo")
        BigDecimal amount,

        @NotBlank(message = "La divisa es obligatoria")
        String currency,

        @NotBlank(message = "El nombre del comercio es obligatorio")
        String merchant,

        @NotNull(message = "El timestamp es obligatorio")
        Instant timestamp
) {}
