package com.victorblasco.fintrack.ingest.domain.repository;

import com.victorblasco.fintrack.ingest.domain.model.CsvBatchAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para operaciones de auditoría sobre {@link CsvBatchAudit}.
 *
 * @author Victor Blasco
 */
@Repository
public interface CsvBatchAuditRepository extends JpaRepository<CsvBatchAudit, Long> {

    /**
     * Recupera todos los registros de auditoría de errores asociados a un identificador de lote.
     *
     * @param batchId identificador del lote CSV
     * @return lista de registros de auditoría de errores
     */
    List<CsvBatchAudit> findByBatchId(UUID batchId);
}
