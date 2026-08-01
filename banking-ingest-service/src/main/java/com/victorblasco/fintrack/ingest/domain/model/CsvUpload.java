package com.victorblasco.fintrack.ingest.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

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

    public CsvUpload() {}

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
