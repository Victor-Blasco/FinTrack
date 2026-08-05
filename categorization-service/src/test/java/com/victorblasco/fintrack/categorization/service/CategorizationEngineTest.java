package com.victorblasco.fintrack.categorization.service;

import com.victorblasco.fintrack.categorization.domain.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
