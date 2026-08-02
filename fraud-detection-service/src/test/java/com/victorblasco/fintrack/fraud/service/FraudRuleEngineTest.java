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
                LocalDateTime.of(2026, 8, 2, 17, 30)
        );

        FraudEvaluationResult result = fraudRuleEngine.evaluate(event);

        assertEquals(Verdict.CLEAN, result.verdict());
        assertEquals(30, result.riskScore());
        assertTrue(result.reasons().contains(FraudReason.HIGH_AMOUNT_ANOMALY));
    }

    @Test
    @DisplayName("Debe dar veredicto SUSPICIOUS para una compra de importe elevado en horario nocturno anómalo (03:30 AM)")
    public void shouldReturnSuspiciousForHighAmountAtOffHours() {
        UUID userId = UUID.randomUUID();
        RawTransactionEvent event = new RawTransactionEvent(
                UUID.randomUUID(),
                userId,
                "ES9121000418451234567891",
                new BigDecimal("2500.00"),
                "EUR",
                "TIENDA NOCTURNA",
                LocalDateTime.of(2026, 8, 2, 3, 30)
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
                LocalDateTime.of(2026, 8, 2, 15, 0)
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
        LocalDateTime now = LocalDateTime.now();

        for (int i = 1; i <= 3; i++) {
            RawTransactionEvent event = new RawTransactionEvent(
                    UUID.randomUUID(),
                    userId,
                    "ES9121000418451234567891",
                    new BigDecimal("15.00"),
                    "EUR",
                    "MERCADONA",
                    now.plusSeconds(i * 5)
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
                now.plusSeconds(25)
        );

        FraudEvaluationResult fourthResult = fraudRuleEngine.evaluate(fourthEvent);

        assertEquals(Verdict.SUSPICIOUS, fourthResult.verdict());
        assertTrue(fourthResult.riskScore() >= 50);
        assertTrue(fourthResult.reasons().contains(FraudReason.HIGH_FREQUENCY_RULE));
    }
}
