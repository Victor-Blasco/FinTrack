package com.victorblasco.fintrack.categorization.service;

import com.victorblasco.fintrack.categorization.domain.Category;
import com.victorblasco.fintrack.categorization.domain.UserCategoryRule;
import com.victorblasco.fintrack.categorization.repository.InMemoryUserCategoryRuleRepository;
import com.victorblasco.fintrack.categorization.repository.UserCategoryRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserCategoryRuleServiceTest {

    private UserCategoryRuleRepository repository;
    private UserCategoryRuleService service;

    @BeforeEach
    public void setUp() {
        repository = new InMemoryUserCategoryRuleRepository();
        service = new UserCategoryRuleServiceImpl(repository);
    }

    @Test
    @DisplayName("Debe crear una regla de categorización personalizada para un usuario correctamente")
    public void shouldCreateRuleSuccessfully() {
        UUID userId = UUID.randomUUID();
        UserCategoryRule rule = service.createRule(userId, "Mercadona", Category.OCIO);

        assertNotNull(rule);
        assertNotNull(rule.id());
        assertEquals(userId, rule.userId());
        assertEquals("Mercadona", rule.pattern());
        assertEquals(Category.OCIO, rule.category());
        assertNotNull(rule.createdAt());

        List<UserCategoryRule> userRules = service.getRulesByUserId(userId);
        assertEquals(1, userRules.size());
        assertEquals(rule.id(), userRules.getFirst().id());
    }

    @Test
    @DisplayName("Debe aislar las reglas por usuario y soportar múltiples reglas por usuario")
    public void shouldIsolateRulesByUser() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        service.createRule(user1, "Cafeteria", Category.ALIMENTACION);
        service.createRule(user1, "Steam", Category.OCIO);
        service.createRule(user2, "Gimnasio", Category.SALUD_DEPORTE);

        List<UserCategoryRule> user1Rules = service.getRulesByUserId(user1);
        List<UserCategoryRule> user2Rules = service.getRulesByUserId(user2);

        assertEquals(2, user1Rules.size());
        assertEquals(1, user2Rules.size());
    }

    @Test
    @DisplayName("Debe eliminar una regla existente del usuario y retornar false si no existe")
    public void shouldDeleteRuleSuccessfully() {
        UUID userId = UUID.randomUUID();
        UserCategoryRule rule = service.createRule(userId, "Gym", Category.SALUD_DEPORTE);

        boolean deleted = service.deleteRule(userId, rule.id());
        assertTrue(deleted);

        List<UserCategoryRule> remaining = service.getRulesByUserId(userId);
        assertTrue(remaining.isEmpty());

        // Intentar eliminar de nuevo
        boolean deletedAgain = service.deleteRule(userId, rule.id());
        assertFalse(deletedAgain);
    }

    @Test
    @DisplayName("Debe lanzar excepción ante parámetros nulos o inválidos al crear regla")
    public void shouldThrowExceptionOnInvalidInput() {
        UUID userId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> service.createRule(null, "Test", Category.OCIO));
        assertThrows(IllegalArgumentException.class, () -> service.createRule(userId, "", Category.OCIO));
        assertThrows(IllegalArgumentException.class, () -> service.createRule(userId, "   ", Category.OCIO));
        assertThrows(IllegalArgumentException.class, () -> service.createRule(userId, "Test", null));
    }
}
