package com.victorblasco.fintrack.profile.consumer;

import com.victorblasco.fintrack.profile.event.TransactionCategorizedEvent;
import com.victorblasco.fintrack.profile.service.LedgerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor Kafka encargado de enriquecer categorías desde el tópico categorized-events.
 */
@Component
public class CategorizedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(CategorizedEventConsumer.class);

    private final LedgerService ledgerService;

    public CategorizedEventConsumer(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @KafkaListener(topics = "categorized-events", groupId = "finance-profile-group")
    public void consume(TransactionCategorizedEvent event) {
        if (event == null || event.transactionId() == null) {
            log.error("[Kafka Consumer: finance-profile] Recibido evento categorizado NULO o sin ID");
            return;
        }
        log.info("[Kafka Consumer: finance-profile] Recibida categoría [{}] para transacción ID [{}]",
                event.category(), event.transactionId());
        try {
            ledgerService.applyCategorization(event);
        } catch (Exception e) {
            log.error("[Kafka Consumer: finance-profile] Error al aplicar categoría ID [{}]: {}",
                    event.transactionId(), e.getMessage(), e);
            throw e;
        }
    }
}
