package com.victorblasco.fintrack.fraud.producer;

import com.victorblasco.fintrack.fraud.event.FraudAlertEvent;
import com.victorblasco.fintrack.fraud.event.FraudVerdictEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Componente productor encendido para publicar veredictos y alertas de fraude a Apache Kafka.
 */
@Component
public class FraudEventProducer {

    private static final Logger log = LoggerFactory.getLogger(FraudEventProducer.class);

    private static final String TOPIC_VERDICTS = "fraud-verdicts";
    private static final String TOPIC_ALERTS = "fraud-alerts";

    private final KafkaTemplate<String, FraudVerdictEvent> verdictKafkaTemplate;
    private final KafkaTemplate<String, FraudAlertEvent> alertKafkaTemplate;

    /**
     * Constructor con inyección de dependencias para los plantillas de Kafka.
     *
     * @param verdictKafkaTemplate plantilla Kafka para veredictos {@link KafkaTemplate}
     * @param alertKafkaTemplate plantilla Kafka para alertas {@link KafkaTemplate}
     */
    public FraudEventProducer(
            KafkaTemplate<String, FraudVerdictEvent> verdictKafkaTemplate,
            KafkaTemplate<String, FraudAlertEvent> alertKafkaTemplate) {
        this.verdictKafkaTemplate = verdictKafkaTemplate;
        this.alertKafkaTemplate = alertKafkaTemplate;
    }

    /**
     * Publica un evento de veredicto al topic {@code fraud-verdicts}.
     *
     * @param event evento de veredicto {@link FraudVerdictEvent}
     */
    public void sendVerdict(FraudVerdictEvent event) {
        log.info("Publicando veredicto de fraude [{}] para transacción [{}] del usuario [{}]",
                event.verdict(), event.transactionId(), event.userId());
        verdictKafkaTemplate.send(TOPIC_VERDICTS, event.transactionId().toString(), event);
    }

    /**
     * Publica un evento de alerta de seguridad al topic {@code fraud-alerts}.
     *
     * @param event evento de alerta {@link FraudAlertEvent}
     */
    public void sendAlert(FraudAlertEvent event) {
        log.warn("ALERTA SEGURIDAD: Publicando alerta prioritaria para transacción [{}] del usuario [{}]",
                event.transactionId(), event.userId());
        alertKafkaTemplate.send(TOPIC_ALERTS, event.userId().toString(), event);
    }
}
