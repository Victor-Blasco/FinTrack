package com.victorblasco.controller;

import com.victorblasco.dto.*;
import com.victorblasco.exception.EmailAlreadyExistsException;
import com.victorblasco.exception.InvalidCredentialsException;
import com.victorblasco.exception.InvalidTokenException;
import com.victorblasco.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    @WithMockUser
    public void shouldRegisterUserSuccessfully() throws Exception {
        UUID userId = UUID.randomUUID();
        String jsonRequest = """
                {
                  "email": "test@fintrack.com",
                  "password": "SecurePassword123"
                }
                """;
        RegisterResponse response = new RegisterResponse(userId, "test@fintrack.com");

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("test@fintrack.com"));
    }

    @Test
    @WithMockUser
    public void shouldReturnConflictWhenEmailExists() throws Exception {
        String jsonRequest = """
                {
                  "email": "test@fintrack.com",
                  "password": "SecurePassword123"
                }
                """;

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("Email already registered: test@fintrack.com"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Email already registered: test@fintrack.com"));
    }

    @Test
    @WithMockUser
    public void shouldLoginSuccessfully() throws Exception {
        UUID userId = UUID.randomUUID();
        String jsonRequest = """
                {
                  "email": "test@fintrack.com",
                  "password": "SecurePassword123"
                }
                """;
        LoginResponse response = new LoginResponse("mocked-jwt-token", userId, "test@fintrack.com");

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("test@fintrack.com"));
    }

    @Test
    @WithMockUser
    public void shouldReturnUnauthorizedWhenLoginCredentialsInvalid() throws Exception {
        String jsonRequest = """
                {
                  "email": "test@fintrack.com",
                  "password": "WrongPassword"
                }
                """;

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @WithMockUser
    public void shouldValidateTokenSuccessfully() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = "valid-jwt-token";
        ValidateResponse response = new ValidateResponse(true, userId);

        when(authService.validateToken("valid-jwt-token")).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/validate")
                        .with(csrf())
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    @WithMockUser
    public void shouldReturnUnauthorizedWhenTokenInvalid() throws Exception {
        String token = "invalid-jwt-token";

        when(authService.validateToken("invalid-jwt-token"))
                .thenThrow(new InvalidTokenException("Invalid or expired token"));

        mockMvc.perform(post("/api/v1/auth/validate")
                        .with(csrf())
                        .param("token", token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }
}
