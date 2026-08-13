package com.victorblasco.fintrack.ingest.service;

import com.victorblasco.fintrack.ingest.dto.RawTransactionEvent;
import com.victorblasco.fintrack.ingest.dto.TransactionManualPayload;
import com.victorblasco.fintrack.ingest.dto.TransactionManualResponse;
import com.victorblasco.fintrack.ingest.producer.RawTransactionProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Servicio de negocio para procesar la ingesta manual de gastos ingresados desde la UI.
 * Mapea las peticiones recibidas hacia eventos {@link RawTransactionEvent} y los transmite a Kafka.
 *
 * @author Victor Blasco
 */
@Service
public class ManualIngestService {

    private static final Logger log = LoggerFactory.getLogger(ManualIngestService.class);

    private final RawTransactionProducer rawTransactionProducer;

    /**
     * Construye el servicio inyectando el componente productor de eventos de Kafka.
     *
     * @param rawTransactionProducer productor de Kafka
     */
    public ManualIngestService(RawTransactionProducer rawTransactionProducer) {
        this.rawTransactionProducer = rawTransactionProducer;
    }

    /**
     * Procesar un gasto ingresado manualmente por el usuario.
     *
     * @param userId identificador del usuario autenticado
     * @param payload DTO con el comercio e importe del gasto
     * @return {@link TransactionManualResponse} con los detalles de la ingesta
     */
    public TransactionManualResponse processManualTransaction(String userId, TransactionManualPayload payload) {
        UUID txUuid = UUID.randomUUID();
        UUID userUuid;
        try {
            userUuid = UUID.fromString(userId);
        } catch (Exception e) {
            userUuid = UUID.nameUUIDFromBytes(userId.getBytes());
        }

        String accountNum = payload.accountNumber() != null && !payload.accountNumber().isBlank()
                ? payload.accountNumber()
                : "ES9121000418450000000000";
        String curr = payload.currency() != null && !payload.currency().isBlank()
                ? payload.currency()
                : "EUR";

        // El importe de un gasto se transmite como un valor negativo en el dominio contable
        BigDecimal amountNegative = payload.amount().negate();
        Instant now = Instant.now();

        log.info("[Kafka Pipeline] Ingestando gasto manual ID {} para usuario {} (Comercio: {}, Importe: {})",
                txUuid, userUuid, payload.merchant(), amountNegative);

        RawTransactionEvent event = new RawTransactionEvent(
                txUuid,
                userUuid,
                accountNum,
                amountNegative,
                curr,
                payload.merchant(),
                now,
                "MANUAL_ENTRY",
                null
        );

        rawTransactionProducer.send(event);

        return new TransactionManualResponse(
                txUuid.toString(),
                "PENDING",
                payload.merchant(),
                payload.amount(),
                now
        );
    }
}
