package com.victorblasco.fintrack.categorization.repository;

import com.victorblasco.fintrack.categorization.domain.UserCategoryRule;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación concurrente en memoria del repositorio de reglas personalizadas de usuario.
 */
@Repository
public class InMemoryUserCategoryRuleRepository implements UserCategoryRuleRepository {

    private final Map<UUID, UserCategoryRule> store = new ConcurrentHashMap<>();

    @Override
    public UserCategoryRule save(UserCategoryRule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("La regla no puede ser nula");
        }
        store.put(rule.id(), rule);
        return rule;
    }

    @Override
    public List<UserCategoryRule> findByUserId(UUID userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return store.values().stream()
                .filter(r -> userId.equals(r.userId()))
                .sorted(Comparator.comparing(UserCategoryRule::createdAt))
                .toList();
    }

    @Override
    public Optional<UserCategoryRule> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<UserCategoryRule> findByIdAndUserId(UUID id, UUID userId) {
        if (id == null || userId == null) {
            return Optional.empty();
        }
        UserCategoryRule rule = store.get(id);
        if (rule != null && userId.equals(rule.userId())) {
            return Optional.of(rule);
        }
        return Optional.empty();
    }

    @Override
    public boolean deleteByIdAndUserId(UUID id, UUID userId) {
        if (id == null || userId == null) {
            return false;
        }
        UserCategoryRule rule = store.get(id);
        if (rule != null && userId.equals(rule.userId())) {
            return store.remove(id) != null;
        }
        return false;
    }

    @Override
    public void deleteAll() {
        store.clear();
    }
}
