package com.victorblasco.fintrack.categorization.consumer;

import com.victorblasco.fintrack.categorization.domain.Category;
import com.victorblasco.fintrack.categorization.domain.Verdict;
import com.victorblasco.fintrack.categorization.event.FraudVerdictEvent;
import com.victorblasco.fintrack.categorization.event.TransactionCategorizedEvent;
import com.victorblasco.fintrack.categorization.producer.CategorizationProducer;
import com.victorblasco.fintrack.categorization.service.CategorizationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class FraudVerdictConsumerTest {

    private CategorizationEngine categorizationEngine;
    private CategorizationProducer categorizationProducer;
    private FraudVerdictConsumer consumer;

    @BeforeEach
    public void setUp() {
        categorizationEngine = mock(CategorizationEngine.class);
        categorizationProducer = mock(CategorizationProducer.class);
        consumer = new FraudVerdictConsumer(categorizationEngine, categorizationProducer);
    }

    @Test
    @DisplayName("Debe ignorar el evento y no publicar nada si el veredicto es SUSPICIOUS")
    public void shouldIgnoreEventWhenVerdictIsSuspicious() {
        FraudVerdictEvent suspiciousEvent = new FraudVerdictEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "CASINO OVERSEAS",
                Verdict.SUSPICIOUS,
                List.of("HIGH_RISK_MERCHANT"),
                LocalDateTime.now()
        );

        consumer.consume(suspiciousEvent);

        verify(categorizationEngine, never()).categorize(any());
        verify(categorizationProducer, never()).sendCategorizedEvent(any());
    }

    @Test
    @DisplayName("Debe manejar de forma defensiva eventos nulos o con veredictos nulos")
    public void shouldHandleNullEventDefensively() {
        consumer.consume(null);
        verify(categorizationEngine, never()).categorize(any());

        FraudVerdictEvent nullVerdictEvent = new FraudVerdictEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "MERCHANT",
                null,
                List.of(),
                LocalDateTime.now()
        );

        consumer.consume(nullVerdictEvent);
        verify(categorizationEngine, never()).categorize(any());
        verify(categorizationProducer, never()).sendCategorizedEvent(any());
    }

    @Test
    @DisplayName("Debe categorizar y publicar el evento enriquecido cuando el veredicto es CLEAN")
    public void shouldCategorizeAndPublishWhenVerdictIsClean() {
        UUID transactionId = UUID.randomUUID();
        FraudVerdictEvent cleanEvent = new FraudVerdictEvent(
                transactionId,
                UUID.randomUUID(),
                "MERCADONA S.A.",
                Verdict.CLEAN,
                List.of(),
                LocalDateTime.now()
        );

        when(categorizationEngine.categorize("MERCADONA S.A.")).thenReturn(Category.ALIMENTACION);

        consumer.consume(cleanEvent);

        verify(categorizationEngine, times(1)).categorize("MERCADONA S.A.");

        ArgumentCaptor<TransactionCategorizedEvent> captor = ArgumentCaptor.forClass(TransactionCategorizedEvent.class);
        verify(categorizationProducer, times(1)).sendCategorizedEvent(captor.capture());

        TransactionCategorizedEvent publishedEvent = captor.getValue();
        assertEquals(transactionId, publishedEvent.transactionId());
        assertEquals(Category.ALIMENTACION, publishedEvent.category());
        assertNotNull(publishedEvent.categorizedAt());
    }
}
