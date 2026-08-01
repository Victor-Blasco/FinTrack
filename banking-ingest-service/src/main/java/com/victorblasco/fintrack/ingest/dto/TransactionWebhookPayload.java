package com.victorblasco.fintrack.ingest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

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
