package com.victorblasco.fintrack.categorization.service;

import com.victorblasco.fintrack.categorization.domain.Category;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Implementación de alto rendimiento del motor de categorización automática de transacciones.
 * <p>
 * Utiliza una estrategia de búsqueda en dos niveles (Two-Pass Lookup):
 * <ol>
 *   <li><b>Fast-Path O(1):</b> Consulta de coincidencias exactas en un {@link HashMap} inmutable con amplio catálogo de marcas.</li>
 *   <li><b>Slow-Path O(N):</b> Respaldo mediante patrones de expresiones regulares precompiladas para palabras clave genéricas.</li>
 * </ol>
 * </p>
 */
@Service
public class CategorizationEngineImpl implements CategorizationEngine {

    private static final Map<String, Category> EXACT_MERCHANT_MAP;
    private static final Map<Pattern, Category> REGEX_PATTERN_MAP;

    static {
        // 1. Fast-Path: Catálogo amplio de coincidencias exactas O(1)
        Map<String, Category> exactMap = new HashMap<>();

        // --- ALIMENTACIÓN ---
        exactMap.put("MERCADONA", Category.ALIMENTACION);
        exactMap.put("CARREFOUR", Category.ALIMENTACION);
        exactMap.put("CARREFOUR EXPRESS", Category.ALIMENTACION);
        exactMap.put("CARREFOUR MARKET", Category.ALIMENTACION);
        exactMap.put("DIA", Category.ALIMENTACION);
        exactMap.put("SUPERMERCADOS DIA", Category.ALIMENTACION);
        exactMap.put("LIDL", Category.ALIMENTACION);
        exactMap.put("ALCAMPO", Category.ALIMENTACION);
        exactMap.put("EROSKI", Category.ALIMENTACION);
        exactMap.put("CONSUM", Category.ALIMENTACION);
        exactMap.put("ALDI", Category.ALIMENTACION);
        exactMap.put("BONPREU", Category.ALIMENTACION);
        exactMap.put("HIPERCOR", Category.ALIMENTACION);
        exactMap.put("SUPERCOR", Category.ALIMENTACION);
        exactMap.put("AHORRAMAS", Category.ALIMENTACION);
        exactMap.put("CAPRABO", Category.ALIMENTACION);
        exactMap.put("EL CORTE INGLES", Category.ALIMENTACION);
        exactMap.put("MAKRO", Category.ALIMENTACION);
        exactMap.put("BM SUPERMERCADOS", Category.ALIMENTACION);
        exactMap.put("GADIS", Category.ALIMENTACION);
        exactMap.put("AMAZON FRESH", Category.ALIMENTACION);
        exactMap.put("GETIR", Category.ALIMENTACION);
        exactMap.put("GOPUFF", Category.ALIMENTACION);

        // --- OCIO, RESTAURACIÓN Y STREAMING ---
        exactMap.put("NETFLIX", Category.OCIO);
        exactMap.put("SPOTIFY", Category.OCIO);
        exactMap.put("HBO MAX", Category.OCIO);
        exactMap.put("DISNEY PLUS", Category.OCIO);
        exactMap.put("AMAZON PRIME", Category.OCIO);
        exactMap.put("STEAM", Category.OCIO);
        exactMap.put("PLAYSTATION", Category.OCIO);
        exactMap.put("NINTENDO", Category.OCIO);
        exactMap.put("XBOX", Category.OCIO);
        exactMap.put("CINESA", Category.OCIO);
        exactMap.put("YELMO CINES", Category.OCIO);
        exactMap.put("TICKETMASTER", Category.OCIO);
        exactMap.put("JUST EAT", Category.OCIO);
        exactMap.put("GLOVO", Category.OCIO);
        exactMap.put("DELIVEROO", Category.OCIO);
        exactMap.put("UBER EATS", Category.OCIO);
        exactMap.put("BURGER KING", Category.OCIO);
        exactMap.put("MCDONALDS", Category.OCIO);
        exactMap.put("KFC", Category.OCIO);
        exactMap.put("DOMINOS PIZZA", Category.OCIO);
        exactMap.put("TELEPIZZA", Category.OCIO);
        exactMap.put("STARBUCKS", Category.OCIO);
        exactMap.put("100 MONTADITOS", Category.OCIO);
        exactMap.put("FOSTER HOLLYWOOD", Category.OCIO);
        exactMap.put("GINOS", Category.OCIO);
        exactMap.put("VIPS", Category.OCIO);
        exactMap.put("LA TAGLIATELLA", Category.OCIO);

        // --- SALUD, DEPORTE Y CUIDADO PERSONAL ---
        exactMap.put("DECATHLON", Category.SALUD_DEPORTE);
        exactMap.put("BASICFIT", Category.SALUD_DEPORTE);
        exactMap.put("BASIC-FIT", Category.SALUD_DEPORTE);
        exactMap.put("FITUP", Category.SALUD_DEPORTE);
        exactMap.put("ALTAFIT", Category.SALUD_DEPORTE);
        exactMap.put("MCFIT", Category.SALUD_DEPORTE);
        exactMap.put("PADEL CLUB", Category.SALUD_DEPORTE);
        exactMap.put("SANITAS", Category.SALUD_DEPORTE);
        exactMap.put("MAPFRE SALUD", Category.SALUD_DEPORTE);
        exactMap.put("ADESLAS", Category.SALUD_DEPORTE);
        exactMap.put("DKV", Category.SALUD_DEPORTE);
        exactMap.put("DRUNI", Category.SALUD_DEPORTE);
        exactMap.put("PRIMOR", Category.SALUD_DEPORTE);
        exactMap.put("SEPHORA", Category.SALUD_DEPORTE);
        exactMap.put("FOOT LOCKER", Category.SALUD_DEPORTE);
        exactMap.put("JD SPORTS", Category.SALUD_DEPORTE);
        exactMap.put("NIKE", Category.SALUD_DEPORTE);
        exactMap.put("ADIDAS", Category.SALUD_DEPORTE);
        exactMap.put("PUMA", Category.SALUD_DEPORTE);

        // --- TRANSPORTE Y MOVILIDAD ---
        exactMap.put("UBER", Category.TRANSPORTE);
        exactMap.put("CABIFY", Category.TRANSPORTE);
        exactMap.put("FREE NOW", Category.TRANSPORTE);
        exactMap.put("BLABLACAR", Category.TRANSPORTE);
        exactMap.put("RENFE", Category.TRANSPORTE);
        exactMap.put("OUIGO", Category.TRANSPORTE);
        exactMap.put("IRYO", Category.TRANSPORTE);
        exactMap.put("METRO MADRID", Category.TRANSPORTE);
        exactMap.put("TMB BARCELONA", Category.TRANSPORTE);
        exactMap.put("EMT", Category.TRANSPORTE);
        exactMap.put("REPSOL", Category.TRANSPORTE);
        exactMap.put("CEPSA", Category.TRANSPORTE);
        exactMap.put("BP", Category.TRANSPORTE);
        exactMap.put("SHELL", Category.TRANSPORTE);
        exactMap.put("GALP", Category.TRANSPORTE);
        exactMap.put("PETROPRIX", Category.TRANSPORTE);
        exactMap.put("BALLENOIL", Category.TRANSPORTE);
        exactMap.put("ZITY", Category.TRANSPORTE);
        exactMap.put("COOLTRA", Category.TRANSPORTE);
        exactMap.put("ACCIONA MOTOS", Category.TRANSPORTE);
        exactMap.put("PARKIMETER", Category.TRANSPORTE);
        exactMap.put("TELPARK", Category.TRANSPORTE);
        exactMap.put("IBERIA", Category.TRANSPORTE);
        exactMap.put("RYANAIR", Category.TRANSPORTE);
        exactMap.put("VUELING", Category.TRANSPORTE);

        // --- VIVIENDA, SERVICIOS Y HOGAR ---
        exactMap.put("IBERDROLA", Category.VIVIENDA);
        exactMap.put("ENDESA", Category.VIVIENDA);
        exactMap.put("NATURGY", Category.VIVIENDA);
        exactMap.put("CANAL DE ISABEL II", Category.VIVIENDA);
        exactMap.put("VODAFONE", Category.VIVIENDA);
        exactMap.put("MOVISTAR", Category.VIVIENDA);
        exactMap.put("ORANGE", Category.VIVIENDA);
        exactMap.put("MASMOVIL", Category.VIVIENDA);
        exactMap.put("YOIGO", Category.VIVIENDA);
        exactMap.put("DIGI", Category.VIVIENDA);
        exactMap.put("LEROY MERLIN", Category.VIVIENDA);
        exactMap.put("IKEA", Category.VIVIENDA);
        exactMap.put("BAUHAUS", Category.VIVIENDA);
        exactMap.put("BRICOMART", Category.VIVIENDA);
        exactMap.put("BRICODEPOT", Category.VIVIENDA);
        exactMap.put("ZARA HOME", Category.VIVIENDA);
        exactMap.put("JYSK", Category.VIVIENDA);

        EXACT_MERCHANT_MAP = Collections.unmodifiableMap(exactMap);

        // 2. Slow-Path: Patrones de expresiones regulares genéricas enriquecidas
        Map<Pattern, Category> regexMap = new LinkedHashMap<>();
        regexMap.put(Pattern.compile("(?i)mercadona|carrefour|(?<![a-zñáéíóú])dia(?![a-zñáéíóú])|lidl|supermercado|hipermercado|fruteria|carniceria|pescaderia|panaderia|charcuteria|grocery|market"), Category.ALIMENTACION);
        regexMap.put(Pattern.compile("(?i)farmacia|gimnasio|dentista|decathlon|doctor|clinica|hospital|optica|psicologo|fisioterapia|crossfit|padel|fitness|sport|spa"), Category.SALUD_DEPORTE);
        regexMap.put(Pattern.compile("(?i)netflix|spotify|cine|bar|restaurante|pub|club|pizzeria|hamburgueseria|heladeria|cafeteria|teatro|concierto|cinema|bowling|bistro|tapas"), Category.OCIO);
        regexMap.put(Pattern.compile("(?i)uber|cabify|renfe|gasolinera|repsol|metro|bus|peaje|parking|aparcamiento|gasoil|gasolina|electrolinera|taxi|autobus|aeropuerto|vueling|iberia|ryanair"), Category.TRANSPORTE);
        regexMap.put(Pattern.compile("(?i)iberdrola|naturgy|alquiler|comunidad|endesa|agua|fontaneria|electricista|cerrajero|inmobiliaria|hipoteca|bricolaje|muebles|limpieza"), Category.VIVIENDA);

        REGEX_PATTERN_MAP = Collections.unmodifiableMap(regexMap);
    }

    @Override
    public Category categorize(String rawMerchant) {
        if (rawMerchant == null || rawMerchant.isBlank()) {
            return Category.OTROS;
        }

        // Paso 1: Normalización rápida (Trim + Uppercase)
        String normalizedMerchant = rawMerchant.trim().toUpperCase();


        // Paso 2: Fast-Path O(1) - Búsqueda exacta en HashMap
        Category exactCategory = EXACT_MERCHANT_MAP.get(normalizedMerchant);
        if (exactCategory != null) {
            return exactCategory;
        }

        // Paso 3: Slow-Path O(N) - Evaluación de reglas regex precompiladas
        for (Map.Entry<Pattern, Category> entry : REGEX_PATTERN_MAP.entrySet()) {
            if (entry.getKey().matcher(normalizedMerchant).find()) {
                return entry.getValue();
            }
        }

        return Category.OTROS;
    }
}
