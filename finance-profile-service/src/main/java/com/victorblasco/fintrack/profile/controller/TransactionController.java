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
            @RequestParam(name = "userId", required = false) UUID userIdQueryParam,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        UUID userId = resolveUserId(userIdQueryParam, authHeader);
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
            @RequestParam(name = "userId", required = false) UUID userIdQueryParam,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        UUID userId = resolveUserId(userIdQueryParam, authHeader);
        TransactionResponse updated = ledgerService.updateTransactionStatus(userId, id, status);
        return ResponseEntity.ok(updated);
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
