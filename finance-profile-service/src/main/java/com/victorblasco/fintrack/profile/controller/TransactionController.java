package com.victorblasco.fintrack.profile.controller;

import com.victorblasco.fintrack.profile.dto.TransactionResponse;
import com.victorblasco.fintrack.profile.service.LedgerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para consultar el historial de movimientos contables y transacciones.
 *
 * @author Victor Blasco
 */
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final LedgerService ledgerService;

    /**
     * Construye el controlador inyectando el servicio de libro mayor.
     *
     * @param ledgerService servicio de negocio del libro mayor
     */
    public TransactionController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    /**
     * Obtiene la lista de movimientos financieros registrados para el usuario.
     *
     * @param userIdQueryParam identificador del usuario en query param
     * @param userIdHeader cabecera HTTP opcional X-User-Id
     * @return respuesta HTTP 200 OK con la lista de {@link TransactionResponse}
     */
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getUserTransactions(
            @RequestParam(name = "userId", required = false) UUID userIdQueryParam,
            @RequestHeader(name = "X-User-Id", required = false) String userIdHeader
    ) {
        UUID userId = resolveUserId(userIdQueryParam, userIdHeader);
        List<TransactionResponse> transactions = ledgerService.getUserTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Resuelve la identidad del usuario dando prioridad al parámetro de consulta o cabecera HTTP.
     *
     * @param queryParam UUID en la URL
     * @param header cabecera HTTP X-User-Id
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
