package com.victorblasco.fintrack.categorization.producer;

import com.victorblasco.fintrack.categorization.event.TransactionCategorizedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Componente productor responsable de publicar eventos {@link TransactionCategorizedEvent}
 * al topic Kafka {@code categorized-events}.
 * <p>
 * Incluye gestión de callbacks asíncronos para auditar la entrega de mensajes.
 * </p>
 */
@Component
public class CategorizationProducer {

    private static final Logger log = LoggerFactory.getLogger(CategorizationProducer.class);
    private static final String TOPIC = "categorized-events";

    private final KafkaTemplate<String, TransactionCategorizedEvent> kafkaTemplate;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param kafkaTemplate plantilla Kafka tipada para eventos de categorización
     */
    public CategorizationProducer(KafkaTemplate<String, TransactionCategorizedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publica un evento de transacción categorizada a Kafka.
     *
     * @param event evento {@link TransactionCategorizedEvent} a emitir
     */
    public void sendCategorizedEvent(TransactionCategorizedEvent event) {
        String key = event.transactionId().toString();
        log.info("Publicando evento categorizado para transacción [{}] con categoría [{}]", key, event.category());
        kafkaTemplate.send(TOPIC, key, event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Error al publicar evento categorizado para transacción [{}]: {}", key, ex.getMessage());
            } else {
                log.debug("Evento categorizado publicado correctamente en offset={}", result.getRecordMetadata().offset());
            }
        });
    }
}
