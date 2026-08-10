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
            @RequestParam(name = "userId", required = false) UUID userIdQueryParam,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        UUID userId = resolveUserId(userIdQueryParam, authHeader);
        AccountSummaryResponse response = ledgerService.getAccountSummary(userId);
        return ResponseEntity.ok(response);
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
