package com.victorblasco.fintrack.ingest.dto;

import java.util.UUID;

public record CsvUploadResponse(
        UUID batchId,
        String status,
        String hash
) {}
