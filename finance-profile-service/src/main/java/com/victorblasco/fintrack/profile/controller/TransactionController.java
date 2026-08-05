package com.victorblasco.fintrack.profile.controller;

import com.victorblasco.fintrack.profile.dto.TransactionResponse;
import com.victorblasco.fintrack.profile.service.LedgerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para el historial de movimientos contables.
 */
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final LedgerService ledgerService;

    public TransactionController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getUserTransactions(
            @RequestParam(name = "userId", required = false) UUID userIdQueryParam,
            @RequestHeader(name = "X-User-Id", required = false) String userIdHeader
    ) {
        UUID userId = resolveUserId(userIdQueryParam, userIdHeader);
        List<TransactionResponse> transactions = ledgerService.getUserTransactions(userId);
        return ResponseEntity.ok(transactions);
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
