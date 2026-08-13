package com.victorblasco.fintrack.profile.consumer;

import com.victorblasco.fintrack.profile.event.RawTransactionEvent;
import com.victorblasco.fintrack.profile.service.LedgerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor Kafka encargado de auditar transacciones crudas desde el tópico raw-transactions.
 */
@Component
public class RawTransactionConsumer {

    private static final Logger log = LoggerFactory.getLogger(RawTransactionConsumer.class);

    private final LedgerService ledgerService;

    public RawTransactionConsumer(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @KafkaListener(topics = "raw-transactions", groupId = "finance-profile-group")
    public void consume(RawTransactionEvent event) {
        if (event == null || event.transactionId() == null) {
            log.error("[Kafka Consumer: finance-profile] Recibido evento raw-transaction NULO o sin ID");
            return;
        }
        log.info("[Kafka Consumer: finance-profile] Recibido evento raw-transaction ID [{}] para usuario [{}] (Comercio: {}, Importe: {} {})",
                event.transactionId(), event.userId(), event.merchant(), event.amount(), event.currency());
        try {
            ledgerService.processRawTransaction(event);
        } catch (Exception e) {
            log.error("[Kafka Consumer: finance-profile] Error al procesar raw-transaction ID [{}]: {}",
                    event.transactionId(), e.getMessage(), e);
            throw e;
        }
    }
}
