package com.victorblasco.fintrack.profile.controller;

import com.victorblasco.fintrack.profile.service.SseEmitterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

/**
 * Controlador REST/SSE para la suscripción a eventos en tiempo real.
 * <p>
 * Expone un flujo Server-Sent Events (SSE) en /api/v1/events/stream para que el frontend
 * reciba notificaciones en vivo sobre cambios de transacciones, saldos y presupuestos.
 * </p>
 *
 * @author Victor Blasco
 */
@RestController
@RequestMapping("/api/v1/events")
public class SseNotificationController {

    private static final Logger log = LoggerFactory.getLogger(SseNotificationController.class);

    private final SseEmitterService sseEmitterService;

    public SseNotificationController(SseEmitterService sseEmitterService) {
        this.sseEmitterService = sseEmitterService;
    }

    /**
     * Inicia una conexión de transmisión de eventos Server-Sent Events (SSE) para el usuario autenticado.
     *
     * @param xUserIdHeader cabecera X-User-Id inyectada por el BFF/Gateway
     * @param userIdQueryParam parámetro opcional de consulta en URL
     * @param authHeader cabecera de autorización opcional
     * @return SseEmitter para la comunicación unidireccional en tiempo real
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents(
            @RequestHeader(name = "X-User-Id", required = false) String xUserIdHeader,
            @RequestParam(name = "userId", required = false) UUID userIdQueryParam,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        UUID userId = resolveUserId(xUserIdHeader, userIdQueryParam, authHeader);
        log.info("Iniciando suscripción SSE para el usuario [{}]", userId);
        return sseEmitterService.subscribe(userId);
    }

    private UUID resolveUserId(String xUserIdHeader, UUID queryParam, String authHeader) {
        if (xUserIdHeader != null && !xUserIdHeader.isBlank()) {
            try {
                return UUID.fromString(xUserIdHeader);
            } catch (IllegalArgumentException ignored) {}
        }
        if (queryParam != null) {
            return queryParam;
        }
        if (authHeader != null && !authHeader.isBlank()) {
            try {
                return UUID.fromString(authHeader);
            } catch (IllegalArgumentException ignored) {}
        }
        throw new IllegalArgumentException("X-User-Id header missing or invalid");
    }
}
