package com.victorblasco.fintrack.ingest.service;

import com.victorblasco.fintrack.ingest.dto.RawTransactionEvent;
import com.victorblasco.fintrack.ingest.dto.TransactionWebhookPayload;
import com.victorblasco.fintrack.ingest.producer.RawTransactionProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Servicio de negocio para procesar webhooks de transacciones en tiempo real en formato PSD2.
 * Mapea las peticiones recibidas hacia eventos {@link RawTransactionEvent} y los transmite a Kafka.
 *
 * @author Victor Blasco
 */
@Service
public class WebhookIngestService {

    private static final Logger log = LoggerFactory.getLogger(WebhookIngestService.class);

    private final RawTransactionProducer rawTransactionProducer;

    /**
     * Construye el servicio inyectando el componente productor de eventos de Kafka.
     *
     * @param rawTransactionProducer productor de Kafka
     */
    public WebhookIngestService(RawTransactionProducer rawTransactionProducer) {
        this.rawTransactionProducer = rawTransactionProducer;
    }

    /**
     * Procesar la carga útil enviada por un webhook bancario PSD2.
     *
     * @param payload objeto DTO con la información de la transacción recibida
     */
    public void processWebhook(TransactionWebhookPayload payload) {
        log.info("[Kafka Pipeline] Procesando webhook PSD2 para transacción {}", payload.transactionId());

        UUID txUuid;
        try {
            txUuid = UUID.fromString(payload.transactionId());
        } catch (Exception e) {
            txUuid = UUID.nameUUIDFromBytes(payload.transactionId().getBytes());
        }

        UUID defaultUserUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        RawTransactionEvent event = new RawTransactionEvent(
                txUuid,
                defaultUserUuid,
                payload.accountNumber(),
                payload.amount(),
                payload.currency(),
                payload.merchant(),
                payload.timestamp(),
                "PSD2_WEBHOOK",
                null
        );

        rawTransactionProducer.send(event);
    }
}
