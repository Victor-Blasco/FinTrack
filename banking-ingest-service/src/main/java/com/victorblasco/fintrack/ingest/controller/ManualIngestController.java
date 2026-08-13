package com.victorblasco.fintrack.ingest.controller;

import com.victorblasco.fintrack.ingest.dto.TransactionManualPayload;
import com.victorblasco.fintrack.ingest.dto.TransactionManualResponse;
import com.victorblasco.fintrack.ingest.service.ManualIngestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para recibir peticiones de ingesta manual de gastos desde el cliente web.
 * Expone el endpoint POST /api/v1/ingest/transaction.
 *
 * @author Victor Blasco
 */
@RestController
@RequestMapping("/api/v1/ingest")
public class ManualIngestController {

    private final ManualIngestService manualIngestService;

    /**
     * Construye el controlador inyectando el servicio de ingesta manual.
     *
     * @param manualIngestService servicio de procesamiento de ingesta manual
     */
    public ManualIngestController(ManualIngestService manualIngestService) {
        this.manualIngestService = manualIngestService;
    }

    /**
     * Endpoint para recibir y registrar un gasto manual introducido por el usuario.
     *
     * @param userId identificador del usuario extraído de la cabecera X-User-Id
     * @param payload objeto DTO con la información del gasto
     * @return {@link ResponseEntity} con código HTTP 202 Accepted y respuesta {@link TransactionManualResponse}
     */
    @PostMapping("/transaction")
    public ResponseEntity<TransactionManualResponse> ingestManual(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "user-default") String userId,
            @Valid @RequestBody TransactionManualPayload payload) {
        TransactionManualResponse response = manualIngestService.processManualTransaction(userId, payload);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
