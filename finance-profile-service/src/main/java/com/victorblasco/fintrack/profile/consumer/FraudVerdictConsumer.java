package com.victorblasco.fintrack.profile.consumer;

import com.victorblasco.fintrack.profile.event.FraudVerdictEvent;
import com.victorblasco.fintrack.profile.service.LedgerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor Kafka encargado de consolidar veredictos de fraude desde el tópico fraud-verdicts.
 */
@Component
public class FraudVerdictConsumer {

    private static final Logger log = LoggerFactory.getLogger(FraudVerdictConsumer.class);

    private final LedgerService ledgerService;

    public FraudVerdictConsumer(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @KafkaListener(topics = "fraud-verdicts", groupId = "finance-profile-group")
    public void consume(FraudVerdictEvent event) {
        log.info("Consumiendo veredicto de fraude [{}] para transacción [{}]", event.verdict(), event.transactionId());
        ledgerService.applyFraudVerdict(event);
    }
}
