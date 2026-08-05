package com.victorblasco.fintrack.profile.service;

import com.victorblasco.fintrack.profile.dto.AccountSummaryResponse;
import com.victorblasco.fintrack.profile.dto.TransactionResponse;
import com.victorblasco.fintrack.profile.event.FraudVerdictEvent;
import com.victorblasco.fintrack.profile.event.RawTransactionEvent;
import com.victorblasco.fintrack.profile.event.TransactionCategorizedEvent;

import java.util.List;
import java.util.UUID;

/**
 * Interfaz del servicio de negocio del libro mayor contable (Ledger).
 * <p>
 * Define las operaciones necesarias para la ingesta inicial de transacciones crudas,
 * consolidación atómica de saldos tras la evaluación de fraude, asignación de categorías
 * y consulta de estados contables para la aplicación cliente.
 * </p>
 *
 * @author Victor Blasco
 */
public interface LedgerService {

    /**
     * Registra una nueva transacción bancaria cruda entrante con estado inicial PENDING.
     *
     * @param event evento {@link RawTransactionEvent} proveniente de Kafka
     */
    void processRawTransaction(RawTransactionEvent event);

    /**
     * Aplica el resultado del motor de detección de fraude sobre una transacción previamente registrada.
     *
     * @param event evento {@link FraudVerdictEvent} con veredicto CLEAN o SUSPICIOUS
     */
    void applyFraudVerdict(FraudVerdictEvent event);

    /**
     * Enriquece la transacción asignando la categoría de gasto determinada.
     *
     * @param event evento {@link TransactionCategorizedEvent} con la categoría asignada
     */
    void applyCategorization(TransactionCategorizedEvent event);

    /**
     * Obtiene el resumen consolidado de la cuenta bancaria y saldo del usuario.
     *
     * @param userId identificador único del usuario
     * @return {@link AccountSummaryResponse} con la información de la cuenta y saldo disponible
     */
    AccountSummaryResponse getAccountSummary(UUID userId);

    /**
     * Obtiene la lista ordenada de transacciones financieras registradas para el usuario.
     *
     * @param userId identificador único del usuario
     * @return lista de DTOs {@link TransactionResponse} ordenados cronológicamente de forma descendente
     */
    List<TransactionResponse> getUserTransactions(UUID userId);
}
