package com.victorblasco.fintrack.profile.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Evento de resultado de evaluación de fraude entrante desde el tópico fraud-verdicts.
 */
public record FraudVerdictEvent(
        UUID transactionId,
        UUID userId,
        String merchant,
        String verdict,
        List<String> reasons,
        LocalDateTime evaluatedAt
) {}
