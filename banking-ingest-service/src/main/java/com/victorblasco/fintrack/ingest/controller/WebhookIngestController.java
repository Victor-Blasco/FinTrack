package com.victorblasco.fintrack.ingest.controller;

import com.victorblasco.fintrack.ingest.dto.TransactionWebhookPayload;
import com.victorblasco.fintrack.ingest.service.WebhookIngestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ingest")
public class WebhookIngestController {

    private final WebhookIngestService webhookIngestService;

    public WebhookIngestController(WebhookIngestService webhookIngestService) {
        this.webhookIngestService = webhookIngestService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveWebhook(@Valid @RequestBody TransactionWebhookPayload payload) {
        webhookIngestService.processWebhook(payload);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
