package com.victorblasco.fintrack.categorization.service;

import com.victorblasco.fintrack.categorization.domain.Category;

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
    Category categorize(String merchant);
}
