package com.victorblasco.fintrack.ingest.controller;

import com.victorblasco.fintrack.ingest.dto.TransactionManualPayload;
import com.victorblasco.fintrack.ingest.dto.TransactionManualResponse;
import com.victorblasco.fintrack.ingest.service.ManualIngestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ManualIngestController.class)
class ManualIngestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManualIngestService manualIngestService;

    @Test
    @DisplayName("DADO un gasto manual válido CUANDO se envía POST /api/v1/ingest/transaction ENTONCES responde HTTP 202 Accepted")
    void shouldAcceptValidManualTransaction() throws Exception {
        String jsonPayload = """
                {
                  "merchant": "MERCADONA VIGO",
                  "amount": 45.50
                }
                """;

        TransactionManualResponse mockResponse = new TransactionManualResponse(
                "tx-man-1234",
                "PENDING",
                "MERCADONA VIGO",
                new BigDecimal("45.50"),
                Instant.now()
        );

        when(manualIngestService.processManualTransaction(eq("user-test-123"), any(TransactionManualPayload.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/ingest/transaction")
                        .header("X-User-Id", "user-test-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.transactionId").value("tx-man-1234"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.merchant").value("MERCADONA VIGO"));
    }

    @Test
    @DisplayName("DADO un gasto manual con importe negativo o vacío CUANDO se envía POST /api/v1/ingest/transaction ENTONCES responde HTTP 400 Bad Request")
    void shouldRejectInvalidManualTransaction() throws Exception {
        String invalidJsonPayload = """
                {
                  "merchant": "",
                  "amount": -10.00
                }
                """;

        mockMvc.perform(post("/api/v1/ingest/transaction")
                        .header("X-User-Id", "user-test-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJsonPayload))
                .andExpect(status().isBadRequest());
    }
}
