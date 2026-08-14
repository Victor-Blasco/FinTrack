package com.victorblasco.fintrack.categorization.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.victorblasco.fintrack.categorization.domain.Category;
import com.victorblasco.fintrack.categorization.domain.UserCategoryRule;
import com.victorblasco.fintrack.categorization.repository.UserCategoryRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Implementación del servicio de reglas de categorización de usuario con soporte de caché Caffeine.
 */
@Service
public class UserCategoryRuleServiceImpl implements UserCategoryRuleService {

    private static final Logger log = LoggerFactory.getLogger(UserCategoryRuleServiceImpl.class);

    private final UserCategoryRuleRepository repository;

    /**
     * Caché Caffeine para mantener en memoria de acceso ultra rápido las reglas por usuario.
     */
    private final Cache<UUID, List<UserCategoryRule>> userRulesCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(50_000)
            .build();

    public UserCategoryRuleServiceImpl(UserCategoryRuleRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserCategoryRule createRule(UUID userId, String pattern, Category category) {
        if (userId == null) {
            throw new IllegalArgumentException("El identificador del usuario no puede ser nulo");
        }
        if (pattern == null || pattern.isBlank()) {
            throw new IllegalArgumentException("El patrón de comercio no puede estar vacío");
        }
        if (category == null) {
            throw new IllegalArgumentException("La categoría no puede ser nula");
        }

        UserCategoryRule rule = new UserCategoryRule(
                UUID.randomUUID(),
                userId,
                pattern.trim(),
                category,
                Instant.now()
        );

        UserCategoryRule saved = repository.save(rule);
        userRulesCache.invalidate(userId);
        log.info("Regla de categorización creada para usuario [{}]: patrón [{}] -> categoría [{}]", userId, pattern, category);
        return saved;
    }

    @Override
    public List<UserCategoryRule> getRulesByUserId(UUID userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return userRulesCache.get(userId, repository::findByUserId);
    }

    @Override
    public boolean deleteRule(UUID userId, UUID ruleId) {
        if (userId == null || ruleId == null) {
            return false;
        }
        boolean deleted = repository.deleteByIdAndUserId(ruleId, userId);
        if (deleted) {
            userRulesCache.invalidate(userId);
            log.info("Regla de categorización [{}] eliminada para usuario [{}]", ruleId, userId);
        }
        return deleted;
    }
}
