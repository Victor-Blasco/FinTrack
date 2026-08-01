package com.victorblasco.fintrack.ingest.controller;

import com.victorblasco.fintrack.ingest.dto.TransactionWebhookPayload;
import com.victorblasco.fintrack.ingest.service.WebhookIngestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para recibir peticiones de Webhook bancarios en tiempo real (PSD2).
 * Expone el endpoint POST /api/v1/ingest/webhook.
 *
 * @author Victor Blasco
 */
@RestController
@RequestMapping("/api/v1/ingest")
public class WebhookIngestController {

    private final WebhookIngestService webhookIngestService;

    /**
     * Construye el controlador inyectando el servicio de ingesta de webhooks.
     *
     * @param webhookIngestService servicio de procesamiento de webhooks
     */
    public WebhookIngestController(WebhookIngestService webhookIngestService) {
        this.webhookIngestService = webhookIngestService;
    }

    /**
     * Endpoint para recibir y procesar un webhook de transacción en formato PSD2.
     *
     * @param payload objeto DTO de la transacción recibida
     * @return {@link ResponseEntity} con código HTTP 201 Created si la validación es correcta
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveWebhook(@Valid @RequestBody TransactionWebhookPayload payload) {
        webhookIngestService.processWebhook(payload);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
