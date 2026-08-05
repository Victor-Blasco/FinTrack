package com.victorblasco.fintrack.profile.producer;

import com.victorblasco.fintrack.profile.event.BudgetAlertEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Productor encendido para publicar eventos de alerta de presupuesto al tópico Kafka budget-alerts.
 */
@Component
public class BudgetAlertProducer {

    private static final Logger log = LoggerFactory.getLogger(BudgetAlertProducer.class);
    private static final String TOPIC = "budget-alerts";

    private final KafkaTemplate<String, BudgetAlertEvent> kafkaTemplate;

    public BudgetAlertProducer(KafkaTemplate<String, BudgetAlertEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendAlert(BudgetAlertEvent event) {
        String key = event.userId().toString();
        log.warn("EMITIENDO ALERTA DE PRESUPUESTO: Nivel [{}] para usuario [{}] en categoría [{}]",
                event.alertLevel(), key, event.category());
        kafkaTemplate.send(TOPIC, key, event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Error al publicar alerta de presupuesto para usuario [{}]: {}", key, ex.getMessage());
            } else {
                log.debug("Alerta de presupuesto publicada correctamente en offset={}", result.getRecordMetadata().offset());
            }
        });
    }
}
