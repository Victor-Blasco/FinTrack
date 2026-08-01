package com.victorblasco.fintrack.ingest.service;

import com.victorblasco.fintrack.ingest.domain.model.CsvBatchAudit;
import com.victorblasco.fintrack.ingest.domain.model.CsvUpload;
import com.victorblasco.fintrack.ingest.domain.repository.CsvBatchAuditRepository;
import com.victorblasco.fintrack.ingest.domain.repository.CsvUploadRepository;
import com.victorblasco.fintrack.ingest.dto.CsvUploadResponse;
import com.victorblasco.fintrack.ingest.dto.RawTransactionEvent;
import com.victorblasco.fintrack.ingest.exception.DuplicateCsvException;
import com.victorblasco.fintrack.ingest.producer.RawTransactionProducer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class CsvIngestService {

    private static final Logger log = LoggerFactory.getLogger(CsvIngestService.class);

    private final CsvUploadRepository csvUploadRepository;
    private final CsvBatchAuditRepository csvBatchAuditRepository;
    private final RawTransactionProducer rawTransactionProducer;

    public CsvIngestService(
            CsvUploadRepository csvUploadRepository,
            CsvBatchAuditRepository csvBatchAuditRepository,
            RawTransactionProducer rawTransactionProducer
    ) {
        this.csvUploadRepository = csvUploadRepository;
        this.csvBatchAuditRepository = csvBatchAuditRepository;
        this.rawTransactionProducer = rawTransactionProducer;
    }

    public CsvUploadResponse processCsvUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo CSV no puede estar vacío");
        }

        String hash = computeSha256Hash(file);
        log.info("Archivo CSV subido originalFilename={}, hash={}", file.getOriginalFilename(), hash);

        if (csvUploadRepository.existsByHash(hash)) {
            log.warn("Rechazando archivo CSV duplicado con hash={}", hash);
            throw new DuplicateCsvException("El archivo CSV ya ha sido procesado previamente");
        }

        UUID batchId = UUID.randomUUID();
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "statement.csv";
        CsvUpload upload = new CsvUpload(batchId, hash, filename, Instant.now());
        csvUploadRepository.save(upload);

        // Programar procesamiento asíncrono del lote
        parseAndStreamCsv(file, batchId);

        return new CsvUploadResponse(batchId, "PROCESSING", hash);
    }

    @Async
    public void parseAndStreamCsv(MultipartFile file, UUID batchId) {
        log.info("Iniciando análisis asíncrono de lotes CSV para batchId={}", batchId);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreSurroundingSpaces(true)
                     .build()
                     .parse(reader)) {

            int rowNumber = 1;
            for (CSVRecord record : csvParser) {
                rowNumber++;
                try {
                    String transactionId = record.get("transactionId");
                    String accountNumber = record.get("accountNumber");
                    BigDecimal amount = new BigDecimal(record.get("amount"));
                    String currency = record.get("currency");
                    String merchant = record.get("merchant");
                    Instant timestamp = Instant.parse(record.get("timestamp"));

                    RawTransactionEvent event = new RawTransactionEvent(
                            transactionId,
                            accountNumber,
                            amount,
                            currency,
                            merchant,
                            timestamp,
                            "CSV_IMPORT",
                            batchId.toString()
                    );

                    rawTransactionProducer.send(event);
                } catch (Exception ex) {
                    log.error("Fila corrupta en batchId={} fila={}: {}", batchId, rowNumber, ex.getMessage());
                    CsvBatchAudit audit = new CsvBatchAudit(
                            batchId,
                            rowNumber,
                            ex.getMessage(),
                            record.toList().toString(),
                            Instant.now()
                    );
                    csvBatchAuditRepository.save(audit);
                }
            }

            log.info("Lote CSV batchId={} procesado completamente", batchId);
        } catch (Exception ex) {
            log.error("Error al leer el archivo CSV para batchId={}: {}", batchId, ex.getMessage());
        }
    }

    private String computeSha256Hash(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(file.getBytes());
            return HexFormat.of().formatHex(hashBytes);
        } catch (Exception e) {
            throw new IllegalStateException("Error al calcular el hash SHA-256 del archivo CSV", e);
        }
    }
}
