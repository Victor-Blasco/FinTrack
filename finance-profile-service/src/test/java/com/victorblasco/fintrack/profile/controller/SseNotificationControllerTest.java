package com.victorblasco.fintrack.profile.controller;

import com.victorblasco.fintrack.profile.service.SseEmitterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SseNotificationController.class)
class SseNotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SseEmitterService sseEmitterService;

    @Test
    @WithMockUser
    @DisplayName("DADO header X-User-Id válido CUANDO se llama /api/v1/events/stream ENTONCES suscribe y retorna stream SSE")
    void shouldSubscribeWithXUserIdHeader() throws Exception {
        UUID userId = UUID.randomUUID();
        SseEmitter emitter = new SseEmitter(1800000L);

        when(sseEmitterService.subscribe(eq(userId))).thenReturn(emitter);

        mockMvc.perform(get("/api/v1/events/stream")
                        .with(csrf())
                        .header("X-User-Id", userId.toString())
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk());

        verify(sseEmitterService).subscribe(eq(userId));
    }

    @Test
    @WithMockUser
    @DisplayName("DADO query param userId válido CUANDO se llama /api/v1/events/stream ENTONCES suscribe y retorna stream SSE")
    void shouldSubscribeWithQueryParam() throws Exception {
        UUID userId = UUID.randomUUID();
        SseEmitter emitter = new SseEmitter(1800000L);

        when(sseEmitterService.subscribe(eq(userId))).thenReturn(emitter);

        mockMvc.perform(get("/api/v1/events/stream")
                        .with(csrf())
                        .param("userId", userId.toString())
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk());

        verify(sseEmitterService).subscribe(eq(userId));
    }
}
