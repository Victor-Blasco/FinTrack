package com.victorblasco.fintrack.categorization.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.victorblasco.fintrack.categorization.domain.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Petición para registrar una nueva regla personalizada de categorización.
 *
 * @param pattern patrón o texto del comercio a buscar
 * @param category categoría asignada
 */
public record CreateRuleRequest(
        @NotBlank(message = "El patrón del comercio no puede estar vacío")
        @JsonAlias({"merchantPattern", "merchant_pattern", "pattern"})
        String pattern,

        @NotNull(message = "La categoría es obligatoria")
        Category category
) {
}
