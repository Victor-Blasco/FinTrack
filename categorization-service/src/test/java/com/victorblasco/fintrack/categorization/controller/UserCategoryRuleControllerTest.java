package com.victorblasco.fintrack.categorization.controller;

import com.victorblasco.fintrack.categorization.domain.Category;
import com.victorblasco.fintrack.categorization.domain.UserCategoryRule;
import com.victorblasco.fintrack.categorization.exception.GlobalExceptionHandler;
import com.victorblasco.fintrack.categorization.service.UserCategoryRuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserCategoryRuleControllerTest {

    private MockMvc mockMvc;
    private UserCategoryRuleService ruleService;

    @BeforeEach
    public void setUp() {
        ruleService = mock(UserCategoryRuleService.class);
        UserCategoryRuleController controller = new UserCategoryRuleController(ruleService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/categorization/rules - Debe crear una regla y retornar 201 Created")
    public void shouldCreateRuleSuccessfully() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID ruleId = UUID.randomUUID();
        Instant now = Instant.now();

        when(ruleService.createRule(eq(userId), eq("Mi Cafeteria"), eq(Category.OCIO)))
                .thenReturn(new UserCategoryRule(ruleId, userId, "Mi Cafeteria", Category.OCIO, now));

        String requestJson = """
                {
                    "pattern": "Mi Cafeteria",
                    "category": "OCIO"
                }
                """;

        mockMvc.perform(post("/api/v1/categorization/rules")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ruleId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.pattern").value("Mi Cafeteria"))
                .andExpect(jsonPath("$.category").value("OCIO"));

        verify(ruleService, times(1)).createRule(eq(userId), eq("Mi Cafeteria"), eq(Category.OCIO));
    }

    @Test
    @DisplayName("GET /api/v1/categorization/rules - Debe listar las reglas del usuario y retornar 200 OK")
    public void shouldGetRulesSuccessfully() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID ruleId = UUID.randomUUID();
        Instant now = Instant.now();

        when(ruleService.getRulesByUserId(eq(userId)))
                .thenReturn(List.of(new UserCategoryRule(ruleId, userId, "Gym", Category.SALUD_DEPORTE, now)));

        mockMvc.perform(get("/api/v1/categorization/rules")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(ruleId.toString()))
                .andExpect(jsonPath("$[0].pattern").value("Gym"))
                .andExpect(jsonPath("$[0].category").value("SALUD_DEPORTE"));

        verify(ruleService, times(1)).getRulesByUserId(eq(userId));
    }

    @Test
    @DisplayName("DELETE /api/v1/categorization/rules/{id} - Debe eliminar la regla y retornar 204 No Content")
    public void shouldDeleteRuleSuccessfully() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID ruleId = UUID.randomUUID();

        when(ruleService.deleteRule(eq(userId), eq(ruleId))).thenReturn(true);

        mockMvc.perform(delete("/api/v1/categorization/rules/{id}", ruleId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isNoContent());

        verify(ruleService, times(1)).deleteRule(eq(userId), eq(ruleId));
    }

    @Test
    @DisplayName("DELETE /api/v1/categorization/rules/{id} - Debe retornar 404 si la regla no existe")
    public void shouldReturn404WhenRuleNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID ruleId = UUID.randomUUID();

        when(ruleService.deleteRule(eq(userId), eq(ruleId))).thenReturn(false);

        mockMvc.perform(delete("/api/v1/categorization/rules/{id}", ruleId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debe retornar 400 Bad Request si no se proporciona cabecera X-User-Id válida")
    public void shouldReturnBadRequestWhenMissingUserId() throws Exception {
        mockMvc.perform(get("/api/v1/categorization/rules"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("X-User-Id header missing or invalid"));
    }

    @Test
    @DisplayName("POST - Debe retornar 400 Bad Request ante cuerpo inválido")
    public void shouldReturnBadRequestOnInvalidBody() throws Exception {
        UUID userId = UUID.randomUUID();
        String invalidJson = """
                {
                    "pattern": "",
                    "category": null
                }
                """;

        mockMvc.perform(post("/api/v1/categorization/rules")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
