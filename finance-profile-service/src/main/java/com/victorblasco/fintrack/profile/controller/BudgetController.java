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
            @RequestParam(name = "userId", required = false) UUID userIdQueryParam,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        UUID userId = resolveUserId(userIdQueryParam, authHeader);
        BudgetResponse response = budgetService.createOrUpdateBudget(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene la lista de presupuestos activos y sus porcentajes de consumo para el usuario.
     */
    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getUserBudgets(
            @RequestParam(name = "userId", required = false) UUID userIdQueryParam,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        UUID userId = resolveUserId(userIdQueryParam, authHeader);
        List<BudgetResponse> budgets = budgetService.getUserBudgets(userId);
        return ResponseEntity.ok(budgets);
    }

    /**
     * Elimina un presupuesto de categoría personalizada por su ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(
            @PathVariable("id") UUID id,
            @RequestParam(name = "userId", required = false) UUID userIdQueryParam,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        UUID userId = resolveUserId(userIdQueryParam, authHeader);
        budgetService.deleteBudget(userId, id);
        return ResponseEntity.noContent().build();
    }

    private UUID resolveUserId(UUID queryParam, String header) {
        if (queryParam != null) {
            return queryParam;
        }
        if (header != null && !header.isBlank()) {
            try {
                return UUID.fromString(header);
            } catch (IllegalArgumentException ignored) {}
        }
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }
}
