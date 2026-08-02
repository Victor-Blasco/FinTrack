package com.victorblasco.fintrack.fraud.consumer;

import com.victorblasco.fintrack.fraud.domain.FraudEvaluationResult;
import com.victorblasco.fintrack.fraud.domain.FraudReason;
import com.victorblasco.fintrack.fraud.domain.Verdict;
import com.victorblasco.fintrack.fraud.event.FraudAlertEvent;
import com.victorblasco.fintrack.fraud.event.FraudVerdictEvent;
import com.victorblasco.fintrack.fraud.event.RawTransactionEvent;
import com.victorblasco.fintrack.fraud.producer.FraudEventProducer;
import com.victorblasco.fintrack.fraud.service.FraudRuleEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RawTransactionConsumerTest {

    private FraudRuleEngine fraudRuleEngine;
    private FraudEventProducer fraudEventProducer;
    private RawTransactionConsumer consumer;

    @BeforeEach
    public void setUp() {
        fraudRuleEngine = mock(FraudRuleEngine.class);
        fraudEventProducer = mock(FraudEventProducer.class);
        consumer = new RawTransactionConsumer(fraudRuleEngine, fraudEventProducer);
    }

    @Test
    @DisplayName("Debe procesar la transacción y publicar solo el veredicto cuando la transacción es CLEAN")
    public void shouldPublishVerdictOnlyWhenClean() {
        RawTransactionEvent rawEvent = new RawTransactionEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "ES9121000418451234567891",
                new BigDecimal("42.50"),
                "EUR",
                "MERCADONA",
                LocalDateTime.now()
        );

        when(fraudRuleEngine.evaluate(any())).thenReturn(
                new FraudEvaluationResult(Verdict.CLEAN, 0, List.of())
        );

        consumer.consume(rawEvent);

        verify(fraudEventProducer, times(1)).sendVerdict(any(FraudVerdictEvent.class));
        verify(fraudEventProducer, never()).sendAlert(any(FraudAlertEvent.class));
    }

    @Test
    @DisplayName("Debe procesar la transacción y publicar tanto el veredicto como la alerta cuando es SUSPICIOUS")
    public void shouldPublishVerdictAndAlertWhenSuspicious() {
        RawTransactionEvent rawEvent = new RawTransactionEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "ES9121000418451234567891",
                new BigDecimal("850.00"),
                "EUR",
                "CASINO OVERSEAS",
                LocalDateTime.now()
        );

        when(fraudRuleEngine.evaluate(any())).thenReturn(
                new FraudEvaluationResult(Verdict.SUSPICIOUS, 70, List.of(FraudReason.HIGH_RISK_MERCHANT))
        );

        consumer.consume(rawEvent);

        ArgumentCaptor<FraudVerdictEvent> verdictCaptor = ArgumentCaptor.forClass(FraudVerdictEvent.class);
        ArgumentCaptor<FraudAlertEvent> alertCaptor = ArgumentCaptor.forClass(FraudAlertEvent.class);

        verify(fraudEventProducer, times(1)).sendVerdict(verdictCaptor.capture());
        verify(fraudEventProducer, times(1)).sendAlert(alertCaptor.capture());

        assertEquals(Verdict.SUSPICIOUS, verdictCaptor.getValue().verdict());
        assertEquals("HIGH_RISK_MERCHANT", alertCaptor.getValue().reasons().getFirst());
    }
}
