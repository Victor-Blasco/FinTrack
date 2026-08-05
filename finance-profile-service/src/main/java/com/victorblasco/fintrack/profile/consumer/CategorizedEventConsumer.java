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
        log.info("Consumiendo evento categorizado [{}] para transacción [{}]", event.category(), event.transactionId());
        ledgerService.applyCategorization(event);
    }
}
