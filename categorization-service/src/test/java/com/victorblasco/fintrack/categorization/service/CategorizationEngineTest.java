package com.victorblasco.fintrack.categorization.service;

import com.victorblasco.fintrack.categorization.domain.Category;
import com.victorblasco.fintrack.categorization.domain.UserCategoryRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CategorizationEngineTest {

    private CategorizationEngine categorizationEngine;

    @BeforeEach
    public void setUp() {
        categorizationEngine = new CategorizationEngineImpl();
    }

    @ParameterizedTest
    @DisplayName("Debe categorizar comercios por coincidencia exacta Fast-Path O(1)")
    @ValueSource(strings = {
            "Mercadona", "carrefour", "DIA", "LIDL", "Alcampo", "Eroski", "Aldi", "Gadis", "Ahorramas",
            "NETFLIX", "SPOTIFY", "HBO MAX", "Disney Plus", "Burger King", "McDonalds", "Glovo", "Uber Eats",
            "Decathlon", "BasicFit", "Sanitas", "Sephora", "Druni",
            "UBER", "Cabify", "Renfe", "Ouigo", "Iryo", "Free Now", "BlaBlaCar", "Ryanair", "Vueling",
            "Iberdrola", "Endesa", "Naturgy", "Digi", "Leroy Merlin", "Ikea"
    })
    public void shouldCategorizeExactFastPath(String merchant) {
        Category category = categorizationEngine.categorize(merchant);
        assertEquals(true, category != Category.OTROS);
    }

    @ParameterizedTest
    @DisplayName("Debe categorizar comercios de alimentación correctamente incluyendo formatos bancarios especiales de DIA")
    @ValueSource(strings = {
            "MERCADONA S.A.",
            "Carrefour Express",
            "Supermercado DIA",
            "SUPERMERCADO DIA.",
            "DIA-EXPRESS",
            "DIA/MADRID",
            "DIA STORE 123",
            "LIDL Supermercados",
            "SUPERMERCADO LOCAL",
            "FRUTERIA MANOLO",
            "PANADERIA LA ESPIGA"
    })
    public void shouldCategorizeAlimentacion(String merchant) {
        Category category = categorizationEngine.categorize(merchant);
        assertEquals(Category.ALIMENTACION, category);
    }

    @ParameterizedTest
    @DisplayName("Debe categorizar comercios de ocio correctamente")
    @ValueSource(strings = {
            "NETFLIX.COM", "Spotify Premium", "Cinesa Diagonal", "Bar Manolo", "Restaurante El Tenedor",
            "PUB EL PIRATA", "Club Nocturno", "Pizzeria Napoli", "Hamburgueseria Bacoa", "Cafeteria Central"
    })
    public void shouldCategorizeOcio(String merchant) {
        Category category = categorizationEngine.categorize(merchant);
        assertEquals(Category.OCIO, category);
    }

    @ParameterizedTest
    @DisplayName("Debe categorizar comercios de salud y deporte correctamente")
    @ValueSource(strings = {
            "FARMACIA SANTA CRUZ", "Gimnasio BasicFit", "Dentista Dr. Perez", "DECATHLON ESPANA",
            "Doctor Consulta", "Clinica Dental", "Hospital San Carlos", "Crossfit Box", "Padel Club"
    })
    public void shouldCategorizeSaludDeporte(String merchant) {
        Category category = categorizationEngine.categorize(merchant);
        assertEquals(Category.SALUD_DEPORTE, category);
    }

    @ParameterizedTest
    @DisplayName("Debe categorizar comercios de transporte correctamente")
    @ValueSource(strings = {
            "UBER *TRIP", "Cabify Ride", "RENFE Ticket", "GASOLINERA CEPSA", "Repsol E.S.",
            "METRO MADRID", "Bus Interurbano", "Peaje AP-6", "Parking Centro", "Taxi Madrid"
    })
    public void shouldCategorizeTransporte(String merchant) {
        Category category = categorizationEngine.categorize(merchant);
        assertEquals(Category.TRANSPORTE, category);
    }

    @ParameterizedTest
    @DisplayName("Debe categorizar comercios de vivienda correctamente")
    @ValueSource(strings = {
            "IBERDROLA CLIENTES", "Naturgy Luz", "Alquiler Piso Julio", "Comunidad de Vecinos",
            "Endesa Energia", "Canal de Isabel II Agua", "Fontaneria URGENTE", "Hipoteca Mensual"
    })
    public void shouldCategorizeVivienda(String merchant) {
        Category category = categorizationEngine.categorize(merchant);
        assertEquals(Category.VIVIENDA, category);
    }

    @ParameterizedTest
    @DisplayName("Debe asignar OTROS cuando el comercio no coincide, contiene subcadenas como Diagonal/Mediodía/Media o es nulo/vacío")
    @ValueSource(strings = {"ZARA CLOTHES", "Amazon EU", "Librería Central", "MEDIODÍA", "RADIO MEDIA", "", "   "})
    public void shouldCategorizeOtros(String merchant) {
        Category category = categorizationEngine.categorize(merchant);
        assertEquals(Category.OTROS, category);
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Debe priorizar las reglas personalizadas del usuario sobre las coincidencias Fast-Path del sistema")
    public void shouldPrioritizeUserRulesOverFastPath() {
        UUID userId = UUID.randomUUID();
        UserCategoryRuleService ruleService = org.mockito.Mockito.mock(UserCategoryRuleService.class);
        CategorizationEngine engineWithRules = new CategorizationEngineImpl(ruleService);

        // Mercadona es ALIMENTACION por defecto, pero el usuario define regla -> OCIO
        org.mockito.Mockito.when(ruleService.getRulesByUserId(userId)).thenReturn(List.of(
                new com.victorblasco.fintrack.categorization.domain.UserCategoryRule(
                        UUID.randomUUID(), userId, "MERCADONA", Category.OCIO, java.time.Instant.now()
                )
        ));

        // Con userId del usuario, devuelve la categoría personalizada OCIO
        Category userCategory = engineWithRules.categorize("MERCADONA", userId);
        assertEquals(Category.OCIO, userCategory);

        // Con otro usuario o null, devuelve la categoría del sistema ALIMENTACION
        Category defaultCategory = engineWithRules.categorize("MERCADONA", null);
        assertEquals(Category.ALIMENTACION, defaultCategory);
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Debe emparejar reglas de usuario ignorando mayúsculas/minúsculas y por coincidencia de subcadena")
    public void shouldMatchUserRulesCaseInsensitiveAndSubstring() {
        UUID userId = UUID.randomUUID();
        UserCategoryRuleService ruleService = org.mockito.Mockito.mock(UserCategoryRuleService.class);
        CategorizationEngine engineWithRules = new CategorizationEngineImpl(ruleService);

        org.mockito.Mockito.when(ruleService.getRulesByUserId(userId)).thenReturn(List.of(
                new com.victorblasco.fintrack.categorization.domain.UserCategoryRule(
                        UUID.randomUUID(), userId, "café de la esquina", Category.OCIO, java.time.Instant.now()
                )
        ));

        Category category = engineWithRules.categorize("PAGO EN CAFÉ DE LA ESQUINA S.L.", userId);
        assertEquals(Category.OCIO, category);
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Debe recurrir al catálogo del sistema si ninguna regla del usuario coincide")
    public void shouldFallbackToSystemCatalogWhenNoUserRuleMatches() {
        UUID userId = UUID.randomUUID();
        UserCategoryRuleService ruleService = org.mockito.Mockito.mock(UserCategoryRuleService.class);
        CategorizationEngine engineWithRules = new CategorizationEngineImpl(ruleService);

        org.mockito.Mockito.when(ruleService.getRulesByUserId(userId)).thenReturn(List.of(
                new com.victorblasco.fintrack.categorization.domain.UserCategoryRule(
                        UUID.randomUUID(), userId, "TIENDA INEXISTENTE", Category.OTROS, java.time.Instant.now()
                )
        ));

        // Netflix coincide con Fast-Path del sistema -> OCIO
        Category category = engineWithRules.categorize("NETFLIX", userId);
        assertEquals(Category.OCIO, category);
    }
}
