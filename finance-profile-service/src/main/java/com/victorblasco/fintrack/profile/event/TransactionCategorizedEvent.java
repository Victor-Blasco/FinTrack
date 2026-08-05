package com.victorblasco.fintrack.profile.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento de enriquecimiento de categoría entrante desde el tópico categorized-events.
 */
public record TransactionCategorizedEvent(
        UUID transactionId,
        String category,
        LocalDateTime categorizedAt
) {}
