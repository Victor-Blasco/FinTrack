package com.victorblasco.fintrack.profile.controller;

import com.victorblasco.fintrack.profile.dto.TransactionResponse;
import com.victorblasco.fintrack.profile.service.LedgerService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para consultar el historial de movimientos contables y actualizar la seguridad.
 *
 * @author Victor Blasco
 */
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final LedgerService ledgerService;

    public TransactionController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    /**
     * Obtiene la lista ordenada de movimientos financieros registrados para el usuario.
     */
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getUserTransactions(
            @RequestHeader(name = "X-User-Id", required = false) String xUserIdHeader,
            @RequestParam(name = "userId", required = false) UUID userIdQueryParam,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        UUID userId = resolveUserId(xUserIdHeader, userIdQueryParam, authHeader);
        List<TransactionResponse> transactions = ledgerService.getUserTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Actualiza el estado de seguridad/cuarentena de una transacción en respuesta a FraudShield.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<TransactionResponse> updateTransactionStatus(
            @PathVariable("id") UUID id,
            @RequestParam("status") String status,
            @RequestHeader(name = "X-User-Id", required = false) String xUserIdHeader,
            @RequestParam(name = "userId", required = false) UUID userIdQueryParam,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        UUID userId = resolveUserId(xUserIdHeader, userIdQueryParam, authHeader);
        TransactionResponse updated = ledgerService.updateTransactionStatus(userId, id, status);
        return ResponseEntity.ok(updated);
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
