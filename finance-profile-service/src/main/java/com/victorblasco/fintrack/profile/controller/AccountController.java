package com.victorblasco.fintrack.profile.controller;

import com.victorblasco.fintrack.profile.dto.AccountSummaryResponse;
import com.victorblasco.fintrack.profile.service.LedgerService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controlador REST para la consulta de información de saldos y cuentas bancarias.
 *
 * @author Victor Blasco
 */
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final LedgerService ledgerService;

    public AccountController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    /**
     * Obtiene el resumen de la cuenta bancaria y saldo consolidado del usuario.
     */
    @GetMapping("/summary")
    public ResponseEntity<AccountSummaryResponse> getAccountSummary(
            @RequestHeader(name = "X-User-Id", required = false) String xUserIdHeader,
            @RequestParam(name = "userId", required = false) UUID userIdQueryParam,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        UUID userId = resolveUserId(xUserIdHeader, userIdQueryParam, authHeader);
        AccountSummaryResponse response = ledgerService.getAccountSummary(userId);
        return ResponseEntity.ok(response);
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
