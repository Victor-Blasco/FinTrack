package com.victorblasco.fintrack.ingest.controller;

import com.victorblasco.fintrack.ingest.dto.CsvUploadResponse;
import com.victorblasco.fintrack.ingest.service.CsvIngestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controlador REST para la ingesta masiva de transacciones mediante subida de archivos CSV.
 * Expone el endpoint POST /api/v1/ingest/csv.
 *
 * @author Victor Blasco
 */
@RestController
@RequestMapping("/api/v1/ingest")
public class CsvIngestController {

    private final CsvIngestService csvIngestService;

    /**
     * Construye el controlador inyectando el servicio de ingesta CSV.
     *
     * @param csvIngestService servicio de procesamiento de archivos CSV
     */
    public CsvIngestController(CsvIngestService csvIngestService) {
        this.csvIngestService = csvIngestService;
    }

    /**
     * Endpoint para recibir y procesar un archivo de extracto bancario en formato CSV.
     *
     * @param file archivo multipart subido desde la vista o cliente API
     * @return {@link ResponseEntity} con {@link CsvUploadResponse} y código HTTP 202 Accepted
     */
    @PostMapping("/csv")
    public ResponseEntity<CsvUploadResponse> uploadCsv(@RequestParam("file") MultipartFile file) {
        CsvUploadResponse response = csvIngestService.processCsvUpload(file);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
