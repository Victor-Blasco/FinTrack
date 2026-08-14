package com.victorblasco.fintrack.categorization.service;

import com.victorblasco.fintrack.categorization.domain.Category;
import com.victorblasco.fintrack.categorization.domain.UserCategoryRule;

import java.util.List;
import java.util.UUID;

/**
 * Servicio para gestionar las reglas de categorización personalizadas definidas por los usuarios.
 */
public interface UserCategoryRuleService {

    /**
     * Registra una nueva regla de categorización personalizada para el usuario indicado.
     *
     * @param userId identificador del usuario
     * @param pattern patrón de texto a emparejar
     * @param category categoría asignada
     * @return regla creada
     */
    UserCategoryRule createRule(UUID userId, String pattern, Category category);

    /**
     * Obtiene la lista de todas las reglas de categorización activas para el usuario dado.
     *
     * @param userId identificador del usuario
     * @return lista de reglas del usuario
     */
    List<UserCategoryRule> getRulesByUserId(UUID userId);

    /**
     * Elimina una regla personalizada perteneciente al usuario dado.
     *
     * @param userId identificador del usuario propietario
     * @param ruleId identificador de la regla
     * @return true si la regla fue encontrada y eliminada, false en caso contrario
     */
    boolean deleteRule(UUID userId, UUID ruleId);
}
