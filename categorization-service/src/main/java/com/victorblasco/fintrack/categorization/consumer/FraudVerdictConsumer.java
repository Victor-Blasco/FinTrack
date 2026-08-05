package com.victorblasco.fintrack.categorization.consumer;

import com.victorblasco.fintrack.categorization.domain.Category;
import com.victorblasco.fintrack.categorization.domain.Verdict;
import com.victorblasco.fintrack.categorization.event.FraudVerdictEvent;
import com.victorblasco.fintrack.categorization.event.TransactionCategorizedEvent;
import com.victorblasco.fintrack.categorization.producer.CategorizationProducer;
import com.victorblasco.fintrack.categorization.service.CategorizationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Consumidor de eventos Kafka que escucha el topic {@code fraud-verdicts}.
 * <p>
 * Procesa únicamente eventos con veredicto {@link Verdict#CLEAN}, omitiendo
 * transacciones {@link Verdict#SUSPICIOUS} y registrando errores ante veredictos
 * no reconocidos o nulos.
 * </p>
 */
@Component
public class FraudVerdictConsumer {

    private static final Logger log = LoggerFactory.getLogger(FraudVerdictConsumer.class);

    private final CategorizationEngine categorizationEngine;
    private final CategorizationProducer categorizationProducer;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param categorizationEngine motor de categorización
     * @param categorizationProducer productor de eventos categorizados
     */
    public FraudVerdictConsumer(CategorizationEngine categorizationEngine, CategorizationProducer categorizationProducer) {
        this.categorizationEngine = categorizationEngine;
        this.categorizationProducer = categorizationProducer;
    }

    /**
     * Escucha eventos de veredicto de fraude del topic {@code fraud-verdicts}.
     *
     * @param event evento {@link FraudVerdictEvent} recibido
     */
    @KafkaListener(topics = "fraud-verdicts", groupId = "categorization-group")
    public void consume(FraudVerdictEvent event) {
        if (event == null || event.verdict() == null) {
            log.error("Evento o veredicto de fraude nulo recibido en el mensaje Kafka");
            return;
        }

        if (event.verdict() == Verdict.CLEAN) {
            log.info("Procesando categorización para transacción CLEAN [{}] en comercio [{}]", event.transactionId(), event.merchant());
            Category category = categorizationEngine.categorize(event.merchant());

            TransactionCategorizedEvent categorizedEvent = new TransactionCategorizedEvent(
                    event.transactionId(),
                    category,
                    LocalDateTime.now()
            );

            categorizationProducer.sendCategorizedEvent(categorizedEvent);
        } else if (event.verdict() == Verdict.SUSPICIOUS) {
            log.warn("Transacción [{}] marcada como SUSPICIOUS. Se omite la categorización.", event.transactionId());
        } else {
            log.error("Veredicto no reconocido o no soportado [{}] para la transacción [{}]", event.verdict(), event.transactionId());
        }
    }
}
