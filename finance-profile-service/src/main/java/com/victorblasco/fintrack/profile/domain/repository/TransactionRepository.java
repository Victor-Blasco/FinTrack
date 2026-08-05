package com.victorblasco.fintrack.profile.domain.repository;

import com.victorblasco.fintrack.profile.domain.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para la entidad {@link Transaction} del libro mayor contable.
 */
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByUserIdOrderByTimestampDesc(UUID userId);

    List<Transaction> findByAccountIdOrderByTimestampDesc(UUID accountId);
}
