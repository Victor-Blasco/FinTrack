package com.victorblasco.fintrack.profile.service;

import com.victorblasco.fintrack.profile.domain.model.Category;
import com.victorblasco.fintrack.profile.domain.model.Status;
import com.victorblasco.fintrack.profile.dto.AccountSummaryResponse;
import com.victorblasco.fintrack.profile.dto.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SseEmitterServiceTest {

    private SseEmitterService sseEmitterService;

    @BeforeEach
    void setUp() {
        sseEmitterService = new SseEmitterService();
    }

    @Test
    @DisplayName("DADO un userId CUANDO se suscribe ENTONCES retorna un SseEmitter y lo almacena")
    void shouldSubscribeUserSuccessfully() {
        UUID userId = UUID.randomUUID();

        SseEmitter emitter = sseEmitterService.subscribe(userId);

        assertThat(emitter).isNotNull();
        assertThat(sseEmitterService.getActiveEmitterCount(userId)).isEqualTo(1);
    }

    @Test
    @DisplayName("DADO un usuario con emisor activo CUANDO se emite transaction-update ENTONCES no produce errores")
    void shouldBroadcastTransactionUpdate() {
        UUID userId = UUID.randomUUID();
        sseEmitterService.subscribe(userId);

        TransactionResponse tx = new TransactionResponse(
                UUID.randomUUID(),
                new BigDecimal("49.99"),
                "EUR",
                "AMAZON",
                Status.PROCESSED,
                Category.OTROS,
                LocalDateTime.now()
        );

        sseEmitterService.broadcastTransactionUpdate(userId, tx);
        assertThat(sseEmitterService.getActiveEmitterCount(userId)).isEqualTo(1);
    }

    @Test
    @DisplayName("DADO un usuario con emisor activo CUANDO se emite balance-update ENTONCES no produce errores")
    void shouldBroadcastBalanceUpdate() {
        UUID userId = UUID.randomUUID();
        sseEmitterService.subscribe(userId);

        AccountSummaryResponse accountSummary = new AccountSummaryResponse(
                UUID.randomUUID(),
                userId,
                "ES1234567890",
                new BigDecimal("2300.50"),
                "EUR"
        );

        sseEmitterService.broadcastBalanceUpdate(userId, accountSummary);
        assertThat(sseEmitterService.getActiveEmitterCount(userId)).isEqualTo(1);
    }

    @Test
    @DisplayName("DADO un emisor cerrado o completado CUANDO se remueve ENTONCES se limpia la lista de emisores")
    void shouldRemoveEmitterProperly() {
        UUID userId = UUID.randomUUID();
        SseEmitter emitter = sseEmitterService.subscribe(userId);
        assertThat(sseEmitterService.getActiveEmitterCount(userId)).isEqualTo(1);

        sseEmitterService.removeEmitter(userId, emitter);
        assertThat(sseEmitterService.getActiveEmitterCount(userId)).isEqualTo(0);
    }

    @Test
    @DisplayName("DADO múltiples usuarios y conexiones concurrentes CUANDO se suscriben ENTONCES se aíslan correctamente")
    void shouldHandleMultipleUsersAndEmitters() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        SseEmitter emitter1A = sseEmitterService.subscribe(user1);
        SseEmitter emitter1B = sseEmitterService.subscribe(user1);
        SseEmitter emitter2 = sseEmitterService.subscribe(user2);

        assertThat(sseEmitterService.getActiveEmitterCount(user1)).isEqualTo(2);
        assertThat(sseEmitterService.getActiveEmitterCount(user2)).isEqualTo(1);

        sseEmitterService.removeEmitter(user1, emitter1A);
        assertThat(sseEmitterService.getActiveEmitterCount(user1)).isEqualTo(1);
        assertThat(sseEmitterService.getActiveEmitterCount(user2)).isEqualTo(1);

        sseEmitterService.removeEmitter(user1, emitter1B);
        assertThat(sseEmitterService.getActiveEmitterCount(user1)).isEqualTo(0);
        assertThat(sseEmitterService.getActiveEmitterCount(user2)).isEqualTo(1);
    }

    @Test
    @DisplayName("DADO un emisor que lanza IOException en send CUANDO se hace broadcast ENTONCES se remueve automáticamente sin fallar")
    void shouldHandleIOExceptionDuringBroadcastGracefully() throws Exception {
        UUID userId = UUID.randomUUID();

        SseEmitter mockEmitter = mock(SseEmitter.class);
        doNothing()
                .doThrow(new IOException("Broken pipe"))
                .when(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));

        SseEmitterService serviceWithMock = new SseEmitterService() {
            @Override
            protected SseEmitter createEmitter() {
                return mockEmitter;
            }
        };

        serviceWithMock.subscribe(userId);
        assertThat(serviceWithMock.getActiveEmitterCount(userId)).isEqualTo(1);

        // Al emitir evento, capturará el IOException del mock y removerá el emisor muerto
        serviceWithMock.broadcast(userId, "test-event", "test-data");
        assertThat(serviceWithMock.getActiveEmitterCount(userId)).isEqualTo(0);
    }
}
