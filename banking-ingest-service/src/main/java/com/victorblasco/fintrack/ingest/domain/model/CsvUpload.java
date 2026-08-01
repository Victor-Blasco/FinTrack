package com.victorblasco.fintrack.ingest.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA que almacena los hashes de archivos CSV procesados para garantizar la deduplicación.
 *
 * @author Victor Blasco
 */
@Entity
@Table(name = "csv_uploads", indexes = {
        @Index(name = "idx_csv_hash", columnList = "hash", unique = true)
})
public class CsvUpload {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String hash;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private Instant uploadedAt;

    /**
     * Constructor por defecto para JPA.
     */
    public CsvUpload() {}

    /**
     * Crea un nuevo registro de subida CSV.
     *
     * @param id identificador único (batchId)
     * @param hash resumen SHA-256 del archivo
     * @param filename nombre original del archivo
     * @param uploadedAt fecha y hora de la subida
     */
    public CsvUpload(UUID id, String hash, String filename, Instant uploadedAt) {
        this.id = id;
        this.hash = hash;
        this.filename = filename;
        this.uploadedAt = uploadedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
