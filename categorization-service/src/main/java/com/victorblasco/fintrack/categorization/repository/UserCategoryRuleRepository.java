package com.victorblasco.fintrack.categorization.repository;

import com.victorblasco.fintrack.categorization.domain.UserCategoryRule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrato de repositorio para el almacenamiento y recuperación de reglas de categorización por usuario.
 */
public interface UserCategoryRuleRepository {

    UserCategoryRule save(UserCategoryRule rule);

    List<UserCategoryRule> findByUserId(UUID userId);

    Optional<UserCategoryRule> findById(UUID id);

    Optional<UserCategoryRule> findByIdAndUserId(UUID id, UUID userId);

    boolean deleteByIdAndUserId(UUID id, UUID userId);

    void deleteAll();
}
