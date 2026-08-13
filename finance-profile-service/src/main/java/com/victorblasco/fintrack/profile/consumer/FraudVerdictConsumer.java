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
        if (event == null || event.transactionId() == null) {
            log.error("[Kafka Consumer: finance-profile] Recibido evento veredicto NULO o sin ID");
            return;
        }
        log.info("[Kafka Consumer: finance-profile] Recibido veredicto de fraude [{}] (Score: {}/100) para transacción ID [{}] de usuario [{}]",
                event.verdict(), event.riskScore(), event.transactionId(), event.userId());
        try {
            ledgerService.applyFraudVerdict(event);
        } catch (Exception e) {
            log.error("[Kafka Consumer: finance-profile] Error al aplicar veredicto de fraude ID [{}]: {}",
                    event.transactionId(), e.getMessage(), e);
            throw e;
        }
    }
}
