package com.victorblasco.fintrack.profile.controller;

import com.victorblasco.fintrack.profile.dto.AccountSummaryResponse;
import com.victorblasco.fintrack.profile.service.LedgerService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controlador REST para la consulta de información de saldos y cuentas bancarias.
 * <p>
 * Expone endpoints consumibles por la aplicación cliente {@code fintrack-web-client}
 * para obtener el estado financiero del usuario.
 * </p>
 *
 * @author Victor Blasco
 */
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final LedgerService ledgerService;

    /**
     * Construye el controlador inyectando el servicio de libro mayor.
     *
     * @param ledgerService servicio de negocio del libro mayor
     */
    public AccountController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    /**
     * Obtiene el resumen de la cuenta bancaria y saldo consolidado del usuario.
     *
     * @param userIdQueryParam identificador del usuario pasado opcionalmente como query param
     * @param authHeader cabecera HTTP estándar de autorización u opcional
     * @return respuesta HTTP 200 OK con {@link AccountSummaryResponse}
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

    /**
     * Resuelve la identidad del usuario dando prioridad al parámetro de consulta o cabecera HTTP.
     *
     * @param queryParam UUID en la URL
     * @param header cabecera HTTP Authorization
     * @return UUID del usuario resuelto
     */
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
