package com.victorblasco.fintrack.categorization.event;

import com.victorblasco.fintrack.categorization.domain.Category;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento emitido al topic Kafka {@code categorized-events} tras procesar la transacción.
 *
 * @param transactionId identificador único de la transacción
 * @param category categoría asignada al gasto
 * @param categorizedAt marca de tiempo de la categorización
 */
public record TransactionCategorizedEvent(
        UUID transactionId,
        Category category,
        LocalDateTime categorizedAt
) {
}
