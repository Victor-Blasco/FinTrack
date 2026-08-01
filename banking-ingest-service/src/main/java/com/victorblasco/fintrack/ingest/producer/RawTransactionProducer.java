package com.victorblasco.fintrack.ingest.producer;

import com.victorblasco.fintrack.ingest.dto.RawTransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class RawTransactionProducer {

    private static final Logger log = LoggerFactory.getLogger(RawTransactionProducer.class);

    private final KafkaTemplate<String, RawTransactionEvent> kafkaTemplate;
    private final String topicName;

    public RawTransactionProducer(
            KafkaTemplate<String, RawTransactionEvent> kafkaTemplate,
            @Value("${app.kafka.topics.raw-transactions:raw-transactions}") String topicName
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    public void send(RawTransactionEvent event) {
        log.info("Publicando evento raw-transaction para transactionId={}", event.transactionId());
        kafkaTemplate.send(topicName, event.transactionId(), event);
    }
}
