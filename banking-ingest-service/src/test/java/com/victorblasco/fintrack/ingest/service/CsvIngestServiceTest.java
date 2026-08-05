package com.victorblasco.fintrack.ingest.service;

import com.victorblasco.fintrack.ingest.domain.model.CsvUpload;
import com.victorblasco.fintrack.ingest.domain.repository.CsvBatchAuditRepository;
import com.victorblasco.fintrack.ingest.domain.repository.CsvUploadRepository;
import com.victorblasco.fintrack.ingest.dto.CsvUploadResponse;
import com.victorblasco.fintrack.ingest.dto.RawTransactionEvent;
import com.victorblasco.fintrack.ingest.exception.DuplicateCsvException;
import com.victorblasco.fintrack.ingest.producer.RawTransactionProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsvIngestServiceTest {

    @Mock
    private CsvUploadRepository csvUploadRepository;

    @Mock
    private CsvBatchAuditRepository csvBatchAuditRepository;

    @Mock
    private RawTransactionProducer rawTransactionProducer;

    private CsvIngestService csvIngestService;

    @BeforeEach
    void setUp() {
        csvIngestService = new CsvIngestService(csvUploadRepository, csvBatchAuditRepository, rawTransactionProducer);
    }

    @Test
    @DisplayName("DADO un archivo CSV único CUANDO se procesa ENTONCES lo guarda en DB y genera CsvUploadResponse")
    void shouldProcessUniqueCsvUpload() {
        String csvContent = "transactionId,accountNumber,amount,currency,merchant,timestamp\n" +
                "trx-101,ES999,120.00,EUR,ZARA,2026-08-01T15:30:00Z";
        MockMultipartFile file = new MockMultipartFile("file", "statement.csv", "text/csv", csvContent.getBytes());

        when(csvUploadRepository.existsByHash(anyString())).thenReturn(false);

        CsvUploadResponse response = csvIngestService.processCsvUpload(file);

        assertThat(response).isNotNull();
        assertThat(response.batchId()).isNotNull();
        assertThat(response.status()).isEqualTo("PROCESSING");
        assertThat(response.hash()).isNotEmpty();

        verify(csvUploadRepository).save(any(CsvUpload.class));
    }

    @Test
    @DisplayName("DADO un archivo CSV ya procesado CUANDO se sube de nuevo ENTONCES lanza DuplicateCsvException")
    void shouldThrowExceptionWhenCsvHashExists() {
        String csvContent = "transactionId,accountNumber,amount,currency,merchant,timestamp\n" +
                "trx-101,ES999,120.00,EUR,ZARA,2026-08-01T15:30:00Z";
        MockMultipartFile file = new MockMultipartFile("file", "statement.csv", "text/csv", csvContent.getBytes());

        when(csvUploadRepository.existsByHash(anyString())).thenReturn(true);

        assertThatThrownBy(() -> csvIngestService.processCsvUpload(file))
                .isInstanceOf(DuplicateCsvException.class)
                .hasMessageContaining("El archivo CSV ya ha sido procesado previamente");

        verify(csvUploadRepository, never()).save(any());
    }

    @Test
    @DisplayName("DADO un CSV con filas válidas e inválidas CUANDO se analiza ENTONCES emite eventos de las válidas y audita los errores de las corruptas sin fallar")
    void shouldStreamCsvRowsAndAuditMalformedRows() {
        String csvContent = "transactionId,accountNumber,amount,currency,merchant,timestamp\n" +
                "trx-201,ES111,85.20,EUR,STRADIVARIUS,2026-08-01T10:00:00Z\n" +
                "CORRUPT_ROW_WITHOUT_COLUMNS\n" +
                "trx-202,ES111,15.00,EUR,REPSOL,2026-08-01T11:00:00Z";
        MockMultipartFile file = new MockMultipartFile("file", "batch.csv", "text/csv", csvContent.getBytes());

        csvIngestService.parseAndStreamCsv(file, java.util.UUID.randomUUID());

        verify(rawTransactionProducer, times(2)).send(any(RawTransactionEvent.class));
        verify(csvBatchAuditRepository, times(1)).saveAll(anyList());
    }
}
