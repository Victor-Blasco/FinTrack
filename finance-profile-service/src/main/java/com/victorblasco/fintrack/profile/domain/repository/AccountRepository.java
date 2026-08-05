package com.victorblasco.fintrack.profile.domain.repository;

import com.victorblasco.fintrack.profile.domain.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para la entidad {@link Account} con soporte de consultas de actualización atómica SQL.
 */
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByUserId(UUID userId);

    Optional<Account> findByAccountNumber(String accountNumber);

    @Modifying
    @Query("UPDATE Account a SET a.balance = a.balance + :amount WHERE a.id = :accountId")
    int updateBalanceAtomic(@Param("accountId") UUID accountId, @Param("amount") BigDecimal amount);
}
