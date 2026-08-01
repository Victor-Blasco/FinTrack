package com.victorblasco.fintrack.ingest.domain.repository;

import com.victorblasco.fintrack.ingest.domain.model.CsvUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface CsvUploadRepository extends JpaRepository<CsvUpload, UUID> {
    boolean existsByHash(String hash);
}
