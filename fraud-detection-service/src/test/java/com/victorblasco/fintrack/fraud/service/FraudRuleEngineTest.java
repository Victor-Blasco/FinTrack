package com.victorblasco.fintrack.fraud.service;

import com.victorblasco.fintrack.fraud.domain.FraudEvaluationResult;
import com.victorblasco.fintrack.fraud.domain.FraudReason;
import com.victorblasco.fintrack.fraud.domain.Verdict;
import com.victorblasco.fintrack.fraud.event.RawTransactionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class FraudRuleEngineTest {

    private FraudRuleEngine fraudRuleEngine;

    @BeforeEach
    public void setUp() {
        fraudRuleEngine = new FraudRuleEngineImpl();
    }

    @Test
    @DisplayName("Debe dar veredicto CLEAN para una compra legítima de importe elevado durante el día (ej: comprar un coche)")
    public void shouldReturnCleanForLegitimateHighAmountCarPurchase() {
        UUID userId = UUID.randomUUID();
        RawTransactionEvent event = new RawTransactionEvent(
                UUID.randomUUID(),
                userId,
                "ES9121000418451234567891",
                new BigDecimal("5000.00"),
                "EUR",
                "CONCESIONARIO MOTOR SEAT",
                java.time.Instant.parse("2026-08-02T17:30:00Z"),
                "PSD2_WEBHOOK",
                null
        );

        FraudEvaluationResult result = fraudRuleEngine.evaluate(event);

        assertEquals(Verdict.CLEAN, result.verdict());
        assertEquals(30, result.riskScore());
        assertTrue(result.reasons().contains(FraudReason.HIGH_AMOUNT_ANOMALY));
    }

    @Test
    @DisplayName("Debe dar veredicto SUSPICIOUS para una compra de importe elevado en horario nocturno anómalo (03:30 AM UTC)")
    public void shouldReturnSuspiciousForHighAmountAtOffHours() {
        UUID userId = UUID.randomUUID();
        RawTransactionEvent event = new RawTransactionEvent(
                UUID.randomUUID(),
                userId,
                "ES9121000418451234567891",
                new BigDecimal("2500.00"),
                "EUR",
                "TIENDA NOCTURNA",
                java.time.Instant.parse("2026-08-02T03:30:00Z"),
                "PSD2_WEBHOOK",
                null
        );

        FraudEvaluationResult result = fraudRuleEngine.evaluate(event);

        assertEquals(Verdict.SUSPICIOUS, result.verdict());
        assertEquals(55, result.riskScore());
        assertTrue(result.reasons().contains(FraudReason.HIGH_AMOUNT_ANOMALY));
        assertTrue(result.reasons().contains(FraudReason.OFF_HOURS_ACTIVITY));
    }

    @Test
    @DisplayName("Debe dar veredicto SUSPICIOUS para una transacción en comercio de alto riesgo (CASINO)")
    public void shouldReturnSuspiciousForHighRiskMerchant() {
        UUID userId = UUID.randomUUID();
        RawTransactionEvent event = new RawTransactionEvent(
                UUID.randomUUID(),
                userId,
                "ES9121000418451234567891",
                new BigDecimal("2500.00"),
                "EUR",
                "CASINO ROYAL ONLINE",
                java.time.Instant.parse("2026-08-02T15:00:00Z"),
                "PSD2_WEBHOOK",
                null
        );

        FraudEvaluationResult result = fraudRuleEngine.evaluate(event);

        assertEquals(Verdict.SUSPICIOUS, result.verdict());
        assertEquals(70, result.riskScore()); // 30 (importe) + 40 (comercio alto riesgo)
        assertTrue(result.reasons().contains(FraudReason.HIGH_AMOUNT_ANOMALY));
        assertTrue(result.reasons().contains(FraudReason.HIGH_RISK_MERCHANT));
    }

    @Test
    @DisplayName("Debe dar veredicto SUSPICIOUS cuando un usuario supera las 3 transacciones en 60 segundos (Alta Frecuencia)")
    public void shouldReturnSuspiciousForHighFrequencyTransactions() {
        UUID userId = UUID.randomUUID();
        java.time.Instant now = java.time.Instant.now();

        for (int i = 1; i <= 3; i++) {
            RawTransactionEvent event = new RawTransactionEvent(
                    UUID.randomUUID(),
                    userId,
                    "ES9121000418451234567891",
                    new BigDecimal("15.00"),
                    "EUR",
                    "MERCADONA",
                    now.plusSeconds(i * 5),
                    "PSD2_WEBHOOK",
                    null
            );
            FraudEvaluationResult res = fraudRuleEngine.evaluate(event);
            assertEquals(Verdict.CLEAN, res.verdict());
        }

        // La 4ª transacción en <60s debe activar HIGH_FREQUENCY_RULE
        RawTransactionEvent fourthEvent = new RawTransactionEvent(
                UUID.randomUUID(),
                userId,
                "ES9121000418451234567891",
                new BigDecimal("15.00"),
                "EUR",
                "MERCADONA",
                now.plusSeconds(25),
                "PSD2_WEBHOOK",
                null
        );

        FraudEvaluationResult fourthResult = fraudRuleEngine.evaluate(fourthEvent);

        assertEquals(Verdict.SUSPICIOUS, fourthResult.verdict());
        assertTrue(fourthResult.riskScore() >= 50);
        assertTrue(fourthResult.reasons().contains(FraudReason.HIGH_FREQUENCY_RULE));
    }

    @Test
    @DisplayName("Debe activar DAILY_SPEND_SPIKE (+30 riesgo) cuando el gasto acumulado en 24h supera los 3.000,00 EUR en compras consecutivas")
    public void shouldTriggerDailySpendSpikeWhenConsecutiveTransactionsExceedThreshold() {
        UUID userId = UUID.randomUUID();
        java.time.Instant start = java.time.Instant.parse("2026-08-02T10:00:00Z");

        // 1ª transacción: 1.800,00 EUR (por debajo de 2.000 y acumula 1.800)
        RawTransactionEvent event1 = new RawTransactionEvent(
                UUID.randomUUID(),
                userId,
                "ES9121000418451234567891",
                new BigDecimal("1800.00"),
                "EUR",
                "TIENDA ELECTRONICA",
                start,
                "PSD2_WEBHOOK",
                null
        );
        FraudEvaluationResult result1 = fraudRuleEngine.evaluate(event1);
        assertEquals(Verdict.CLEAN, result1.verdict());
        assertEquals(0, result1.riskScore());
        assertFalse(result1.reasons().contains(FraudReason.DAILY_SPEND_SPIKE));

        // 2ª transacción en la misma ventana de 24h: 1.500,00 EUR (acumulado = 3.300,00 EUR > 3.000)
        RawTransactionEvent event2 = new RawTransactionEvent(
                UUID.randomUUID(),
                userId,
                "ES9121000418451234567891",
                new BigDecimal("1500.00"),
                "EUR",
                "TIENDA ROPA",
                start.plusSeconds(3600), // 1 hora después
                "PSD2_WEBHOOK",
                null
        );
        FraudEvaluationResult result2 = fraudRuleEngine.evaluate(event2);
        assertEquals(30, result2.riskScore());
        assertTrue(result2.reasons().contains(FraudReason.DAILY_SPEND_SPIKE));
    }

    @Test
    @DisplayName("No debe activar DAILY_SPEND_SPIKE si el gasto acumulado en 24h no supera 3.000,00 EUR")
    public void shouldNotTriggerDailySpendSpikeWhenTotalSpendIsWithinLimit() {
        UUID userId = UUID.randomUUID();
        java.time.Instant start = java.time.Instant.parse("2026-08-02T10:00:00Z");

        RawTransactionEvent event1 = new RawTransactionEvent(
                UUID.randomUUID(),
                userId,
                "ES9121000418451234567891",
                new BigDecimal("1200.00"),
                "EUR",
                "TIENDA 1",
                start,
                "PSD2_WEBHOOK",
                null
        );
        fraudRuleEngine.evaluate(event1);

        RawTransactionEvent event2 = new RawTransactionEvent(
                UUID.randomUUID(),
                userId,
                "ES9121000418451234567891",
                new BigDecimal("1300.00"),
                "EUR",
                "TIENDA 2",
                start.plusSeconds(7200),
                "PSD2_WEBHOOK",
                null
        );
        FraudEvaluationResult result2 = fraudRuleEngine.evaluate(event2);
        assertEquals(Verdict.CLEAN, result2.verdict());
        assertEquals(0, result2.riskScore());
        assertFalse(result2.reasons().contains(FraudReason.DAILY_SPEND_SPIKE));
    }

    @Test
    @DisplayName("Debe descartar transacciones fuera de la ventana de 24 horas para el cálculo de gasto acumulado")
    public void shouldEvictOldTransactionsOutside24HourWindow() {
        UUID userId = UUID.randomUUID();
        java.time.Instant day1 = java.time.Instant.parse("2026-08-01T10:00:00Z");

        RawTransactionEvent event1 = new RawTransactionEvent(
                UUID.randomUUID(),
                userId,
                "ES9121000418451234567891",
                new BigDecimal("2000.00"),
                "EUR",
                "TIENDA 1",
                day1,
                "PSD2_WEBHOOK",
                null
        );
        fraudRuleEngine.evaluate(event1);

        // Transacción 26 horas después (día siguiente, fuera de la ventana de 24h)
        RawTransactionEvent event2 = new RawTransactionEvent(
                UUID.randomUUID(),
                userId,
                "ES9121000418451234567891",
                new BigDecimal("1500.00"),
                "EUR",
                "TIENDA 2",
                day1.plusSeconds(26 * 3600),
                "PSD2_WEBHOOK",
                null
        );
        FraudEvaluationResult result2 = fraudRuleEngine.evaluate(event2);
        assertEquals(Verdict.CLEAN, result2.verdict());
        assertEquals(0, result2.riskScore());
        assertFalse(result2.reasons().contains(FraudReason.DAILY_SPEND_SPIKE));
    }

    @Test
    @DisplayName("Debe dar veredicto SUSPICIOUS cuando DAILY_SPEND_SPIKE (+30) se combina con actividad nocturna (+25) >= 50")
    public void shouldReturnSuspiciousWhenDailySpendSpikeCombinesWithOffHours() {
        UUID userId = UUID.randomUUID();
        java.time.Instant daytime = java.time.Instant.parse("2026-08-02T16:00:00Z");

        // 1ª compra de día: 1.800 EUR
        RawTransactionEvent event1 = new RawTransactionEvent(
                UUID.randomUUID(),
                userId,
                "ES9121000418451234567891",
                new BigDecimal("1800.00"),
                "EUR",
                "TIENDA DIA",
                daytime,
                "PSD2_WEBHOOK",
                null
        );
        fraudRuleEngine.evaluate(event1);

        // 2ª compra a las 03:30 AM (madrugada, off-hours): 1.500 EUR (total = 3.300 EUR)
        java.time.Instant offHoursNight = java.time.Instant.parse("2026-08-03T03:30:00Z");
        RawTransactionEvent event2 = new RawTransactionEvent(
                UUID.randomUUID(),
                userId,
                "ES9121000418451234567891",
                new BigDecimal("1500.00"),
                "EUR",
                "TIENDA NOCTURNA",
                offHoursNight,
                "PSD2_WEBHOOK",
                null
        );

        FraudEvaluationResult result2 = fraudRuleEngine.evaluate(event2);
        assertEquals(Verdict.SUSPICIOUS, result2.verdict());
        assertEquals(55, result2.riskScore()); // 30 (DAILY_SPEND_SPIKE) + 25 (OFF_HOURS_ACTIVITY)
        assertTrue(result2.reasons().contains(FraudReason.DAILY_SPEND_SPIKE));
        assertTrue(result2.reasons().contains(FraudReason.OFF_HOURS_ACTIVITY));
    }
}
