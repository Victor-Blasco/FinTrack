package com.victorblasco.fintrack.profile.controller;

import com.victorblasco.fintrack.profile.dto.BudgetResponse;
import com.victorblasco.fintrack.profile.dto.CreateBudgetRequest;
import com.victorblasco.fintrack.profile.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para la administración de presupuestos y límites de consumo por categoría.
 */
@RestController
@RequestMapping("/api/v1/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            @Valid @RequestBody CreateBudgetRequest request,
            @RequestParam(name = "userId", required = false) UUID userIdQueryParam,
            @RequestHeader(name = "X-User-Id", required = false) String userIdHeader
    ) {
        UUID userId = resolveUserId(userIdQueryParam, userIdHeader);
        BudgetResponse response = budgetService.createOrUpdateBudget(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getUserBudgets(
            @RequestParam(name = "userId", required = false) UUID userIdQueryParam,
            @RequestHeader(name = "X-User-Id", required = false) String userIdHeader
    ) {
        UUID userId = resolveUserId(userIdQueryParam, userIdHeader);
        List<BudgetResponse> budgets = budgetService.getUserBudgets(userId);
        return ResponseEntity.ok(budgets);
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
