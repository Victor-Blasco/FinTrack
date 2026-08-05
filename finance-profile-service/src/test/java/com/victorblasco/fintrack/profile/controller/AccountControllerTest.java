package com.victorblasco.fintrack.profile.controller;

import com.victorblasco.fintrack.profile.dto.AccountSummaryResponse;
import com.victorblasco.fintrack.profile.service.LedgerService;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LedgerService ledgerService;

    @Test
    @WithMockUser
    public void shouldReturnAccountSummarySuccessfully() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AccountSummaryResponse response = new AccountSummaryResponse(accountId, userId, "ES1234567890", new BigDecimal("1250.80"), "EUR");

        when(ledgerService.getAccountSummary(any(UUID.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/accounts/summary")
                        .with(csrf())
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.balance").value(1250.80))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }
}
