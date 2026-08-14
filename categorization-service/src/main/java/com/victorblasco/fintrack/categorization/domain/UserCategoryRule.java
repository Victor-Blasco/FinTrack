package com.victorblasco.fintrack.categorization.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa una regla de categorización personalizada definida por un usuario.
 *
 * @param id identificador único de la regla
 * @param userId identificador del usuario propietario de la regla
 * @param pattern texto o patrón de comercio a emparejar (subcadena insensible a mayúsculas/minúsculas)
 * @param category categoría de gasto que se asignará al comercio coincidente
 * @param createdAt fecha y hora de creación de la regla
 */
public record UserCategoryRule(
        UUID id,
        UUID userId,
        String pattern,
        Category category,
        Instant createdAt
) {
    public UserCategoryRule {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
