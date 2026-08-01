package com.victorblasco.fintrack.ingest.controller;

import com.victorblasco.fintrack.ingest.dto.TransactionWebhookPayload;
import com.victorblasco.fintrack.ingest.service.WebhookIngestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookIngestController.class)
class WebhookIngestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WebhookIngestService webhookIngestService;

    @Test
    @DisplayName("DADO un webhook PSD2 válido CUANDO se envía POST /api/v1/ingest/webhook ENTONCES responde HTTP 201 Created")
    void shouldAcceptValidWebhook() throws Exception {
        String jsonPayload = """
                {
                  "transactionId": "trx-12345",
                  "accountNumber": "ES12345678901234567890",
                  "amount": 45.50,
                  "currency": "EUR",
                  "merchant": "MERCADONA VIGO",
                  "timestamp": "2026-08-01T20:00:00Z"
                }
                """;

        doNothing().when(webhookIngestService).processWebhook(any(TransactionWebhookPayload.class));

        mockMvc.perform(post("/api/v1/ingest/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated());

        verify(webhookIngestService).processWebhook(any(TransactionWebhookPayload.class));
    }

    @Test
    @DisplayName("DADO un webhook con datos inválidos CUANDO se envía POST /api/v1/ingest/webhook ENTONCES responde HTTP 400 Bad Request")
    void shouldRejectInvalidWebhookPayload() throws Exception {
        String invalidJsonPayload = """
                {
                  "transactionId": "",
                  "accountNumber": "ES12345678901234567890",
                  "amount": -10.00,
                  "currency": "EUR",
                  "merchant": "MERCADONA VIGO",
                  "timestamp": "2026-08-01T20:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/v1/ingest/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJsonPayload))
                .andExpect(status().isBadRequest());
    }
}
