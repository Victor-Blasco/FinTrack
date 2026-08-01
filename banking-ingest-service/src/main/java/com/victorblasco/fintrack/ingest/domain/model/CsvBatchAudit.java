package com.victorblasco.fintrack.ingest.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "csv_batches_audit", indexes = {
        @Index(name = "idx_batch_id", columnList = "batchId")
})
public class CsvBatchAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID batchId;

    @Column(nullable = false)
    private int rowNumber;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String rawContent;

    @Column(nullable = false)
    private Instant loggedAt;

    public CsvBatchAudit() {}

    public CsvBatchAudit(UUID batchId, int rowNumber, String errorMessage, String rawContent, Instant loggedAt) {
        this.batchId = batchId;
        this.rowNumber = rowNumber;
        this.errorMessage = errorMessage;
        this.rawContent = rawContent;
        this.loggedAt = loggedAt;
    }

    public Long getId() {
        return id;
    }

    public UUID getBatchId() {
        return batchId;
    }

    public void setBatchId(UUID batchId) {
        this.batchId = batchId;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getRawContent() {
        return rawContent;
    }

    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }

    public Instant getLoggedAt() {
        return loggedAt;
    }

    public void setLoggedAt(Instant loggedAt) {
        this.loggedAt = loggedAt;
    }
}
