package com.victorblasco.fintrack.ingest.domain.repository;

import com.victorblasco.fintrack.ingest.domain.model.CsvUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

/**
 * Repositorio JPA para operaciones de persistencia y deduplicación sobre {@link CsvUpload}.
 *
 * @author Victor Blasco
 */
@Repository
public interface CsvUploadRepository extends JpaRepository<CsvUpload, UUID> {

    /**
     * Comprueba si ya existe un archivo CSV procesado con el mismo resumen SHA-256.
     *
     * @param hash resumen SHA-256 a consultar
     * @return {@code true} si el hash ya existe en la base de datos, {@code false} en caso contrario
     */
    boolean existsByHash(String hash);
}
