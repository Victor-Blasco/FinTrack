package com.victorblasco.fintrack.ingest.producer;

import com.victorblasco.fintrack.ingest.dto.RawTransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Productor de eventos de Apache Kafka para publicar transacciones sin procesar al tópico raw-transactions.
 * <p>
 * Incluye gestión de callbacks asíncronos para auditar la entrega de mensajes y gestionar posibles errores de red.
 * </p>
 *
 * @author Victor Blasco
 */
@Component
public class RawTransactionProducer {

    private static final Logger log = LoggerFactory.getLogger(RawTransactionProducer.class);

    private final KafkaTemplate<String, RawTransactionEvent> kafkaTemplate;
    private final String topicName;

    /**
     * Construye el productor inyectando la plantilla de Kafka y el nombre del tópico configurado.
     *
     * @param kafkaTemplate plantilla para publicación en Kafka
     * @param topicName nombre del tópico configurado en las propiedades
     */
    public RawTransactionProducer(
            KafkaTemplate<String, RawTransactionEvent> kafkaTemplate,
            @Value("${app.kafka.topics.raw-transactions:raw-transactions}") String topicName
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    /**
     * Publica un evento {@link RawTransactionEvent} utilizando el transactionId como clave de particionamiento.
     *
     * @param event evento de transacción a publicar
     */
    public void send(RawTransactionEvent event) {
        log.info("Publicando evento raw-transaction para transactionId={}", event.transactionId());
        kafkaTemplate.send(topicName, event.transactionId(), event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Fallo al publicar evento raw-transaction para transactionId={}: {}", event.transactionId(), ex.getMessage());
            } else {
                log.debug("Evento raw-transaction publicado exitosamente en offset={}", result.getRecordMetadata().offset());
            }
        });
    }
}
