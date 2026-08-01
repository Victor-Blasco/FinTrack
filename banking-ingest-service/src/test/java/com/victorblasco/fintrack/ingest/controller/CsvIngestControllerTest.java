package com.victorblasco.fintrack.ingest.controller;

import com.victorblasco.fintrack.ingest.dto.CsvUploadResponse;
import com.victorblasco.fintrack.ingest.exception.DuplicateCsvException;
import com.victorblasco.fintrack.ingest.service.CsvIngestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CsvIngestController.class)
class CsvIngestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CsvIngestService csvIngestService;

    @Test
    @DisplayName("DADO un archivo CSV nuevo CUANDO se envía POST /api/v1/ingest/csv ENTONCES responde HTTP 202 Accepted con batchId")
    void shouldAcceptValidCsvUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "statement.csv",
                "text/csv",
                "transactionId,accountNumber,amount,currency,merchant,timestamp\ntrx-1,ES123,50.00,EUR,AMAZON,2026-08-01T12:00:00Z".getBytes()
        );

        UUID batchId = UUID.randomUUID();
        CsvUploadResponse response = new CsvUploadResponse(batchId, "PROCESSING", "sha256-hash-sample");

        when(csvIngestService.processCsvUpload(any())).thenReturn(response);

        mockMvc.perform(multipart("/api/v1/ingest/csv").file(file))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.batchId").value(batchId.toString()))
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    @DisplayName("DADO un archivo CSV duplicado CUANDO se sube de nuevo ENTONCES responde HTTP 409 Conflict")
    void shouldRejectDuplicateCsvUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "duplicate.csv",
                "text/csv",
                "transactionId,accountNumber,amount,currency,merchant,timestamp\ntrx-1,ES123,50.00,EUR,AMAZON,2026-08-01T12:00:00Z".getBytes()
        );

        when(csvIngestService.processCsvUpload(any())).thenThrow(new DuplicateCsvException("El archivo CSV ya ha sido procesado previamente"));

        mockMvc.perform(multipart("/api/v1/ingest/csv").file(file))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
