package com.victorblasco.fintrack.fraud.consumer;

import com.victorblasco.fintrack.fraud.domain.FraudEvaluationResult;
import com.victorblasco.fintrack.fraud.domain.FraudReason;
import com.victorblasco.fintrack.fraud.domain.Verdict;
import com.victorblasco.fintrack.fraud.event.FraudAlertEvent;
import com.victorblasco.fintrack.fraud.event.FraudVerdictEvent;
import com.victorblasco.fintrack.fraud.event.RawTransactionEvent;
import com.victorblasco.fintrack.fraud.producer.FraudEventProducer;
import com.victorblasco.fintrack.fraud.service.FraudRuleEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Consumidor de eventos Kafka que escucha el topic {@code raw-transactions}.
 * <p>
 * Procesa cada evento en tiempo real, invoca el motor {@link FraudRuleEngine}
 * y publica los veredictos y alertas resultantes a través de {@link FraudEventProducer}.
 * </p>
 */
@Component
public class RawTransactionConsumer {

    private static final Logger log = LoggerFactory.getLogger(RawTransactionConsumer.class);

    private final FraudRuleEngine fraudRuleEngine;
    private final FraudEventProducer fraudEventProducer;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param fraudRuleEngine motor de evaluación de reglas de seguridad
     * @param fraudEventProducer productor de veredictos y alertas en Kafka
     */
    public RawTransactionConsumer(FraudRuleEngine fraudRuleEngine, FraudEventProducer fraudEventProducer) {
        this.fraudRuleEngine = fraudRuleEngine;
        this.fraudEventProducer = fraudEventProducer;
    }

    /**
     * Método oyente de Kafka para procesar transacciones bancarias del topic {@code raw-transactions}.
     *
     * @param event evento de transacción cruda {@link RawTransactionEvent}
     */
    @KafkaListener(topics = "raw-transactions", groupId = "fraud-detection-group")
    public void consume(RawTransactionEvent event) {
        log.info("Recibida transacción [{}] de usuario [{}] por importe [{}] EUR en comercio [{}]",
                event.transactionId(), event.userId(), event.amount(), event.merchant());

        FraudEvaluationResult result = fraudRuleEngine.evaluate(event);

        List<String> reasonStrings = result.reasons().stream()
                .map(FraudReason::name)
                .toList();

        FraudVerdictEvent verdictEvent = new FraudVerdictEvent(
                event.transactionId(),
                event.userId(),
                result.verdict(),
                reasonStrings,
                LocalDateTime.now()
        );

        fraudEventProducer.sendVerdict(verdictEvent);

        if (result.verdict() == Verdict.SUSPICIOUS) {
            FraudAlertEvent alertEvent = new FraudAlertEvent(
                    event.transactionId(),
                    event.userId(),
                    event.amount(),
                    event.merchant(),
                    reasonStrings,
                    LocalDateTime.now()
            );
            fraudEventProducer.sendAlert(alertEvent);
        }
    }
}
