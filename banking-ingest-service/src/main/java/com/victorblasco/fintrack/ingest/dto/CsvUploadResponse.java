package com.victorblasco.fintrack.ingest.dto;

import java.util.UUID;

/**
 * Registro DTO de respuesta para confirmación de recepción de subida de archivo CSV.
 *
 * @param batchId identificador único asignado al lote de transacciones
 * @param status estado actual del procesamiento (ej. PROCESSING)
 * @param hash resumen SHA-256 del archivo subido para deduplicación
 *
 * @author Victor Blasco
 */
public record CsvUploadResponse(
        UUID batchId,
        String status,
        String hash
) {}
