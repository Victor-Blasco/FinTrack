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

/**
 * Servicio de negocio encargado de la ingesta masiva de extractos bancarios en formato CSV.
 * <p>
 * Incluye cálculo de resumen SHA-256 para deduplicación estricta, respuesta rápida de procesamiento
 * asíncrono y parseo en streaming línea a línea auditando filas corruptas.
 * </p>
 *
 * @author Victor Blasco
 */
@Service
public class CsvIngestService {

    private static final Logger log = LoggerFactory.getLogger(CsvIngestService.class);

    private final CsvUploadRepository csvUploadRepository;
    private final CsvBatchAuditRepository csvBatchAuditRepository;
    private final RawTransactionProducer rawTransactionProducer;

    /**
     * Construye el servicio inyectando los repositorios de persistencia y el productor de eventos Kafka.
     *
     * @param csvUploadRepository repositorio de deduplicación CSV
     * @param csvBatchAuditRepository repositorio de auditoría de errores de lotes
     * @param rawTransactionProducer productor de Kafka
     */
    public CsvIngestService(
            CsvUploadRepository csvUploadRepository,
            CsvBatchAuditRepository csvBatchAuditRepository,
            RawTransactionProducer rawTransactionProducer
    ) {
        this.csvUploadRepository = csvUploadRepository;
        this.csvBatchAuditRepository = csvBatchAuditRepository;
        this.rawTransactionProducer = rawTransactionProducer;
    }

    /**
     * Procesa la solicitud inicial de subida de un archivo CSV.
     * <p>
     * Calcula el resumen SHA-256 del contenido. Si el resumen ya existe en la base de datos,
     * lanza {@link DuplicateCsvException}. En caso contrario, registra la subida y programa
     * el análisis asíncrono.
     * </p>
     *
     * @param file archivo multipart en formato CSV enviado por el cliente
     * @return {@link CsvUploadResponse} con el identificador del lote asignado y el estado inicial
     * @throws DuplicateCsvException si el archivo ya fue subido previamente
     * @throws IllegalArgumentException si el archivo está vacío o es nulo
     */
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

    /**
     * Lee y analiza asíncronamente el archivo CSV línea a línea en streaming.
     * <p>
     * Por cada fila válida, publica un evento {@link RawTransactionEvent} en Kafka.
     * Si una fila está malformada, guarda un registro en {@link CsvBatchAudit} y continúa con las demás filas.
     * </p>
     *
     * @param file archivo multipart CSV
     * @param batchId identificador del lote
     */
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

    /**
     * Calcula el hash SHA-256 del contenido completo de un archivo MultipartFile.
     *
     * @param file archivo del cual calcular el hash
     * @return cadena de texto hexadecimal con el hash SHA-256
     */
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
