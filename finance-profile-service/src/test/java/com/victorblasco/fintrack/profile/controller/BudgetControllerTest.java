package com.victorblasco.fintrack.profile.controller;

import com.victorblasco.fintrack.profile.domain.model.Category;
import com.victorblasco.fintrack.profile.dto.BudgetResponse;
import com.victorblasco.fintrack.profile.dto.CreateBudgetRequest;
import com.victorblasco.fintrack.profile.service.BudgetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BudgetController.class)
public class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BudgetService budgetService;

    @Test
    @WithMockUser
    public void shouldCreateBudgetSuccessfully() throws Exception {
        UUID budgetId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        String jsonRequest = """
                {
                  "category": "ALIMENTACION",
                  "monthlyLimit": 450.00
                }
                """;

        BudgetResponse response = new BudgetResponse(budgetId, userId, Category.ALIMENTACION, new BigDecimal("450.00"), BigDecimal.ZERO, 0.0);

        when(budgetService.createOrUpdateBudget(eq(userId), any(CreateBudgetRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/budgets")
                        .with(csrf())
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.budgetId").value(budgetId.toString()))
                .andExpect(jsonPath("$.category").value("ALIMENTACION"))
                .andExpect(jsonPath("$.monthlyLimit").value(450.00));
    }
}
