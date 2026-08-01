package com.victorblasco.fintrack.ingest.domain.repository;

import com.victorblasco.fintrack.ingest.domain.model.CsvBatchAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface CsvBatchAuditRepository extends JpaRepository<CsvBatchAudit, Long> {
    List<CsvBatchAudit> findByBatchId(UUID batchId);
}
