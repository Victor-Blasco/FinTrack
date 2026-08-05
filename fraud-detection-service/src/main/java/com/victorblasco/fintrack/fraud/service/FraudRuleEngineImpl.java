package com.victorblasco.fintrack.fraud.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.victorblasco.fintrack.fraud.domain.FraudEvaluationResult;
import com.victorblasco.fintrack.fraud.domain.FraudReason;
import com.victorblasco.fintrack.fraud.domain.Verdict;
import com.victorblasco.fintrack.fraud.event.RawTransactionEvent;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Implementación de alto rendimiento del motor de puntuación de riesgo (Fraud Risk Score) en tiempo real.
 * <p>
 * Emplea una caché delimitada de auto-evicción (Caffeine Cache) para rastrear la frecuencia de transacciones
 * por usuario en ventanas de 60 segundos con consumo de memoria acotado $O(1)$ y evalúa factores de riesgo
 * contextuales sin asignación innecesaria de objetos en la ruta crítica de ejecución.
 * </p>
 */
@Service
public class FraudRuleEngineImpl implements FraudRuleEngine {

    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("2000.00");
    private static final int FREQUENCY_WINDOW_SECONDS = 60;
    private static final int FREQUENCY_MAX_TRANSACTIONS = 3;
    private static final int RISK_THRESHOLD = 50;

    private static final Set<String> HIGH_RISK_KEYWORDS = Set.of(
            // Casas de Apuestas y Juegos de Azar
            "CASINO", "BETTING", "APUESTAS", "POKER", "LOTERIA", "GAMBLING", "SLOTS",
            "BWIN", "BET365", "POKERSTARS", "888SPORT", "CODERE", "LUCKIA", "SPORTIUM", "JACKPOT",

            // Criptomonedas y Exchanges de Criptoactivos
            "CRYPTO", "BINANCE", "COINBASE", "KRAKEN", "BITCOIN", "BYBIT", "KUCOIN",
            "OKX", "BITPANDA", "BITGET", "GATE.IO", "METAMASK", "OPENSEA", "PAXFUL",

            // Remesas Internacionales y Envíos de Dinero / Tarjetas Prepago
            "OVERSEAS", "WESTERN_UNION", "MONEYGRAM", "OFFSHORE", "TRANSFER_ANONYMOUS", "PAYPAL_GIFT",
            "RIA_MONEY", "REMITLY", "WORLDREMIT", "PAYSAFECARD", "STEAM_CARD", "GIFT_CARD_RELOAD",

            // Retiradas de Efectivo Anómalas
            "ATM_UNKNOWN", "ATM_FOREIGN", "ATM_OFFSHORE", "CASH_ADVANCE", "CASH_DISPENSER",

            // Entidades No Verificadas, Servidores de Anonimato y Darkweb
            "DARKWEB", "UNVERIFIED_MERCHANT", "LUXURY_DUTY_FREE", "TOR_NODE", "VPN_ANONYMOUS", "MIXER", "TUMBLER"
    );

    /**
     * Caché delimitada de auto-evicción con TTL de 60 segundos post-acceso para prevenir fugas de memoria (OOM).
     */
    private final Cache<UUID, Deque<Instant>> userFrequencyCache = Caffeine.newBuilder()
            .expireAfterAccess(60, TimeUnit.SECONDS)
            .maximumSize(100_000)
            .build();

    /**
     * Evalúa la transacción bancaria y determina el veredicto en función de la puntuación acumulada.
     *
     * @param event transacción bancaria a evaluar {@link RawTransactionEvent}
     * @return resultado con veredicto, puntuación acumulada y razones de riesgo {@link FraudEvaluationResult}
     */
    @Override
    public FraudEvaluationResult evaluate(RawTransactionEvent event) {
        int riskScore = 0;
        List<FraudReason> reasons = new ArrayList<>();

        Instant now = event.timestamp() != null
                ? event.timestamp().toInstant(ZoneOffset.UTC)
                : Instant.now();

        // 1. Evaluación de Alta Frecuencia (Ventana deslizante de 60 segundos con evicción automática)
        if (checkHighFrequency(event.userId(), now)) {
            riskScore += 50;
            reasons.add(FraudReason.HIGH_FREQUENCY_RULE);
        }

        // 2. Evaluación de Comercio de Alto Riesgo (Bucle directo sin asignación de Stream)
        if (isHighRiskMerchant(event.merchant())) {
            riskScore += 40;
            reasons.add(FraudReason.HIGH_RISK_MERCHANT);
        }

        // 3. Actividad en Horario Nocturno Anómalo (01:00 AM - 05:59 AM)
        if (isOffHours(event.timestamp())) {
            riskScore += 25;
            reasons.add(FraudReason.OFF_HOURS_ACTIVITY);
        }

        // 4. Anomalía de Importe Muy Elevado (> 2.000,00 EUR)
        if (event.amount() != null && event.amount().compareTo(HIGH_AMOUNT_THRESHOLD) > 0) {
            riskScore += 30;
            reasons.add(FraudReason.HIGH_AMOUNT_ANOMALY);
        }

        Verdict verdict = (riskScore >= RISK_THRESHOLD) ? Verdict.SUSPICIOUS : Verdict.CLEAN;
        return new FraudEvaluationResult(verdict, riskScore, reasons);
    }

    /**
     * Verifica la ventana deslizante de frecuencia para un usuario en los últimos 60 segundos.
     */
    private boolean checkHighFrequency(UUID userId, Instant eventInstant) {
        Deque<Instant> timestamps = userFrequencyCache.get(userId, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            Instant cutoff = eventInstant.minusSeconds(FREQUENCY_WINDOW_SECONDS);
            while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
                timestamps.pollFirst();
            }
            timestamps.addLast(eventInstant);
            return timestamps.size() > FREQUENCY_MAX_TRANSACTIONS;
        }
    }

    /**
     * Comprueba si el nombre del comercio contiene palabras clave asociadas a actividades de alto riesgo.
     * <p>
     * Evaluado mediante un bucle directo para evitar asignaciones de iteradores y streams en el Heap.
     * </p>
     */
    private boolean isHighRiskMerchant(String merchant) {
        if (merchant == null || merchant.isBlank()) {
            return false;
        }
        String upperMerchant = merchant.toUpperCase(Locale.ROOT);
        for (String keyword : HIGH_RISK_KEYWORDS) {
            if (upperMerchant.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Evalúa si la transacción ocurre en horario nocturno anómalo (entre la 01:00 AM y las 05:59 AM).
     */
    private boolean isOffHours(LocalDateTime timestamp) {
        if (timestamp == null) {
            return false;
        }
        int hour = timestamp.getHour();
        return hour >= 1 && hour < 6;
    }
}
