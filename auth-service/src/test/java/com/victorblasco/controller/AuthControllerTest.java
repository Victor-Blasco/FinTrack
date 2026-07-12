package com.victorblasco.controller;

import tools.jackson.databind.ObjectMapper;
import com.victorblasco.dto.RegisterRequest;
import com.victorblasco.dto.RegisterResponse;
import com.victorblasco.dto.LoginRequest;
import com.victorblasco.dto.LoginResponse;
import com.victorblasco.dto.ValidateResponse;
import com.victorblasco.exception.EmailAlreadyExistsException;
import com.victorblasco.exception.InvalidCredentialsException;
import com.victorblasco.exception.InvalidTokenException;
import com.victorblasco.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    public void shouldRegisterUserSuccessfully() throws Exception {
        UUID userId = UUID.randomUUID();
        RegisterRequest request = new RegisterRequest("test@fintrack.com", "SecurePassword123");
        RegisterResponse response = new RegisterResponse(userId, "test@fintrack.com", LocalDateTime.now());

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("test@fintrack.com"));
    }

    @Test
    @WithMockUser
    public void shouldReturnConflictWhenEmailExists() throws Exception {
        RegisterRequest request = new RegisterRequest("test@fintrack.com", "SecurePassword123");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("Email already registered: test@fintrack.com"));

        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Email already registered: test@fintrack.com"));
    }

    @Test
    @WithMockUser
    public void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest("test@fintrack.com", "SecurePassword123");
        LoginResponse response = new LoginResponse("mocked-jwt-token", "Bearer", 3600);

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mocked-jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    @WithMockUser
    public void shouldReturnUnauthorizedWhenLoginCredentialsInvalid() throws Exception {
        LoginRequest request = new LoginRequest("test@fintrack.com", "WrongPassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @WithMockUser
    public void shouldValidateTokenSuccessfully() throws Exception {
        String token = "Bearer valid-jwt-token";
        ValidateResponse response = new ValidateResponse("user-id-123", "ROLE_USER");

        when(authService.validate("Bearer valid-jwt-token")).thenReturn(response);

        mockMvc.perform(get("/api/v1/auth/validate") // Usar GET
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-id-123"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    @WithMockUser
    public void shouldReturnUnauthorizedWhenTokenInvalid() throws Exception {
        String token = "Bearer invalid-jwt-token";

        when(authService.validate("Bearer invalid-jwt-token"))
                .thenThrow(new InvalidTokenException("Invalid or expired token"));

        mockMvc.perform(get("/api/v1/auth/validate") // Usar GET
                .header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }
}

