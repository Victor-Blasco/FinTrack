package com.victorblasco.fintrack.categorization.service;

import com.victorblasco.fintrack.categorization.domain.Category;

import java.util.UUID;

/**
 * Interfaz para el motor de categorización automática de transacciones.
 */
public interface CategorizationEngine {

    /**
     * Evalúa el nombre o descripción del comercio y determina su categoría de gasto correspondiente.
     *
     * @param merchant cadena con el nombre/identificador del comercio
     * @return {@link Category} correspondiente
     */
    default Category categorize(String merchant) {
        return categorize(merchant, null);
    }

    /**
     * Evalúa el nombre del comercio y el identificador de usuario, aplicando en primer lugar
     * las reglas personalizadas del usuario antes del catálogo del sistema.
     *
     * @param merchant cadena con el nombre/identificador del comercio
     * @param userId identificador del usuario (puede ser null)
     * @return {@link Category} correspondiente
     */
    Category categorize(String merchant, UUID userId);
}

