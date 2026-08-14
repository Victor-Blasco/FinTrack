package com.victorblasco.fintrack.categorization.controller;

import com.victorblasco.fintrack.categorization.domain.UserCategoryRule;
import com.victorblasco.fintrack.categorization.dto.CreateRuleRequest;
import com.victorblasco.fintrack.categorization.service.UserCategoryRuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para la gestión de reglas de categorización personalizadas por usuario.
 */
@RestController
@RequestMapping("/api/v1/categorization/rules")
public class UserCategoryRuleController {

    private final UserCategoryRuleService ruleService;

    public UserCategoryRuleController(UserCategoryRuleService ruleService) {
        this.ruleService = ruleService;
    }

    /**
     * Registra una nueva regla de categorización personalizada para el usuario autenticado.
     */
    @PostMapping
    public ResponseEntity<UserCategoryRule> createRule(
            @Valid @RequestBody CreateRuleRequest request,
            @RequestHeader(name = "X-User-Id", required = false) String xUserIdHeader,
            @RequestParam(name = "userId", required = false) UUID userIdQueryParam,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        UUID userId = resolveUserId(xUserIdHeader, userIdQueryParam, authHeader);
        UserCategoryRule createdRule = ruleService.createRule(userId, request.pattern(), request.category());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRule);
    }

    /**
     * Obtiene el listado de reglas de categorización activas del usuario.
     */
    @GetMapping
    public ResponseEntity<List<UserCategoryRule>> getRules(
            @RequestHeader(name = "X-User-Id", required = false) String xUserIdHeader,
            @RequestParam(name = "userId", required = false) UUID userIdQueryParam,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        UUID userId = resolveUserId(xUserIdHeader, userIdQueryParam, authHeader);
        List<UserCategoryRule> rules = ruleService.getRulesByUserId(userId);
        return ResponseEntity.ok(rules);
    }

    /**
     * Elimina una regla personalizada perteneciente al usuario actual por su identificador.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(
            @PathVariable("id") UUID id,
            @RequestHeader(name = "X-User-Id", required = false) String xUserIdHeader,
            @RequestParam(name = "userId", required = false) UUID userIdQueryParam,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        UUID userId = resolveUserId(xUserIdHeader, userIdQueryParam, authHeader);
        boolean deleted = ruleService.deleteRule(userId, id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    private UUID resolveUserId(String xUserIdHeader, UUID queryParam, String authHeader) {
        if (xUserIdHeader != null && !xUserIdHeader.isBlank()) {
            try {
                return UUID.fromString(xUserIdHeader.trim());
            } catch (IllegalArgumentException ignored) {}
        }
        if (queryParam != null) {
            return queryParam;
        }
        if (authHeader != null && !authHeader.isBlank()) {
            try {
                return UUID.fromString(authHeader.trim());
            } catch (IllegalArgumentException ignored) {}
        }
        throw new IllegalArgumentException("X-User-Id header missing or invalid");
    }
}
