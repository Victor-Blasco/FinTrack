package com.victorblasco.fintrack.profile.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Servicio encargado de gestionar las conexiones Server-Sent Events (SSE)
 * y difundir actualizaciones en tiempo real a los clientes conectados por usuario.
 *
 * @author Victor Blasco
 */
@Service
public class SseEmitterService {

    private static final Logger log = LoggerFactory.getLogger(SseEmitterService.class);
    private static final long SSE_TIMEOUT_MS = 1_800_000L; // 30 minutos (1.800.000 ms)

    private final Map<UUID, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /**
     * Registra una nueva suscripción SSE para el usuario especificado.
     *
     * @param userId identificador único del usuario
     * @return instancia de SseEmitter configurada con callbacks de ciclo de vida
     */
    public SseEmitter subscribe(UUID userId) {
        SseEmitter emitter = createEmitter();

        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> {
            log.debug("Conexión SSE completada para usuario [{}]", userId);
            removeEmitter(userId, emitter);
        });

        emitter.onTimeout(() -> {
            log.debug("Conexión SSE expirada por timeout para usuario [{}]", userId);
            emitter.complete();
            removeEmitter(userId, emitter);
        });

        emitter.onError(e -> {
            log.debug("Error en conexión SSE para usuario [{}]: {}", userId, e != null ? e.getMessage() : "Desconocido");
            removeEmitter(userId, emitter);
        });

        try {
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data("Conexión SSE establecida"));
        } catch (IOException e) {
            log.warn("Error enviando evento inicial INIT para usuario [{}]: {}", userId, e.getMessage());
            removeEmitter(userId, emitter);
        }

        log.info("Cliente SSE suscrito para usuario [{}]. Total emisores activos para el usuario: {}",
                userId, emitters.getOrDefault(userId, new CopyOnWriteArrayList<>()).size());

        return emitter;
    }

    /**
     * Emite un evento de actualización de transacción a todos los clientes conectados del usuario.
     *
     * @param userId identificador único del usuario
     * @param data datos del movimiento contable actualizado
     */
    public void broadcastTransactionUpdate(UUID userId, Object data) {
        broadcast(userId, "transaction-update", data);
    }

    /**
     * Emite un evento de actualización de saldo a todos los clientes conectados del usuario.
     *
     * @param userId identificador único del usuario
     * @param data datos del resumen de saldos de la cuenta
     */
    public void broadcastBalanceUpdate(UUID userId, Object data) {
        broadcast(userId, "balance-update", data);
    }

    /**
     * Difunde un evento genérico con un nombre de evento a los emisores activos del usuario.
     *
     * @param userId identificador único del usuario
     * @param eventName nombre del evento SSE (ej. transaction-update, balance-update, budget-update)
     * @param data cuerpo serializable del evento
     */
    public void broadcast(UUID userId, String eventName, Object data) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null || userEmitters.isEmpty()) {
            log.debug("No hay emisores SSE registrados para el usuario [{}] al intentar emitir [{}]", userId, eventName);
            return;
        }

        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data, MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                log.warn("Fallo al enviar evento SSE [{}] al usuario [{}]: {}", eventName, userId, e.getMessage());
                removeEmitter(userId, emitter);
            }
        }
    }

    /**
     * Remueve un emisor inactivo o desconectado y limpia la entrada del mapa si no quedan suscriptores.
     *
     * @param userId identificador del usuario
     * @param emitter emisor a remover
     */
    public void removeEmitter(UUID userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(userId, userEmitters);
            }
        }
    }

    /**
     * Obtiene el total de emisores activos para un usuario determinado.
     *
     * @param userId identificador del usuario
     * @return número de emisores activos
     */
    public int getActiveEmitterCount(UUID userId) {
        CopyOnWriteArrayList<SseEmitter> userEmitters = emitters.get(userId);
        return userEmitters != null ? userEmitters.size() : 0;
    }

    /**
     * Tarea periódica programada para enviar un evento liviano de latido (Heartbeat / Ping)
     * cada 25 segundos a todas las conexiones activas, previniendo el cierre por inactividad
     * por parte de proxies intermedios o firewalls.
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 25000)
    public void sendHeartbeats() {
        if (emitters.isEmpty()) {
            return;
        }
        for (Map.Entry<UUID, CopyOnWriteArrayList<SseEmitter>> entry : emitters.entrySet()) {
            UUID userId = entry.getKey();
            for (SseEmitter emitter : entry.getValue()) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("ping")
                            .data("{\"type\":\"HEARTBEAT\"}", MediaType.APPLICATION_JSON));
                } catch (Exception e) {
                    log.debug("Eliminando emisor inactivo durante heartbeat para usuario [{}]", userId);
                    removeEmitter(userId, emitter);
                }
            }
        }
    }

    /**
     * Método fábrica para permitir sobrescritura en tests si fuera necesario.
     */
    protected SseEmitter createEmitter() {
        return new SseEmitter(SSE_TIMEOUT_MS);
    }
}
