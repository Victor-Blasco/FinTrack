package com.victorblasco.fintrack.ingest.controller;

import com.victorblasco.fintrack.ingest.dto.CsvUploadResponse;
import com.victorblasco.fintrack.ingest.service.CsvIngestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/ingest")
public class CsvIngestController {

    private final CsvIngestService csvIngestService;

    public CsvIngestController(CsvIngestService csvIngestService) {
        this.csvIngestService = csvIngestService;
    }

    @PostMapping("/csv")
    public ResponseEntity<CsvUploadResponse> uploadCsv(@RequestParam("file") MultipartFile file) {
        CsvUploadResponse response = csvIngestService.processCsvUpload(file);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
