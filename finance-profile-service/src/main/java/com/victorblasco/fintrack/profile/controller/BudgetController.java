package com.victorblasco.fintrack.profile.controller;

import com.victorblasco.fintrack.profile.dto.BudgetResponse;
import com.victorblasco.fintrack.profile.dto.CreateBudgetRequest;
import com.victorblasco.fintrack.profile.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para la administración de presupuestos y límites de consumo por categoría.
 *
 * @author Victor Blasco
 */
@RestController
@RequestMapping("/api/v1/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    /**
     * Crea o actualiza un presupuesto de gasto mensual para una categoría especificada.
     */
    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            @Valid @RequestBody CreateBudgetRequest request,
            @RequestHeader(name = "X-User-Id", required = false) String xUserIdHeader,
            @RequestParam(name = "userId", required = false) UUID userIdQueryParam,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        UUID userId = resolveUserId(xUserIdHeader, userIdQueryParam, authHeader);
        BudgetResponse response = budgetService.createOrUpdateBudget(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene la lista de presupuestos activos y sus porcentajes de consumo para el usuario.
     */
    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getUserBudgets(
            @RequestHeader(name = "X-User-Id", required = false) String xUserIdHeader,
            @RequestParam(name = "userId", required = false) UUID userIdQueryParam,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        UUID userId = resolveUserId(xUserIdHeader, userIdQueryParam, authHeader);
        List<BudgetResponse> budgets = budgetService.getUserBudgets(userId);
        return ResponseEntity.ok(budgets);
    }

    /**
     * Elimina un presupuesto de categoría personalizada por su ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(
            @PathVariable("id") UUID id,
            @RequestHeader(name = "X-User-Id", required = false) String xUserIdHeader,
            @RequestParam(name = "userId", required = false) UUID userIdQueryParam,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        UUID userId = resolveUserId(xUserIdHeader, userIdQueryParam, authHeader);
        budgetService.deleteBudget(userId, id);
        return ResponseEntity.noContent().build();
    }

    private UUID resolveUserId(String xUserIdHeader, UUID queryParam, String authHeader) {
        if (xUserIdHeader != null && !xUserIdHeader.isBlank()) {
            try {
                return UUID.fromString(xUserIdHeader);
            } catch (IllegalArgumentException ignored) {}
        }
        if (queryParam != null) {
            return queryParam;
        }
        if (authHeader != null && !authHeader.isBlank()) {
            try {
                return UUID.fromString(authHeader);
            } catch (IllegalArgumentException ignored) {}
        }
        throw new IllegalArgumentException("X-User-Id header missing or invalid");
    }
}
