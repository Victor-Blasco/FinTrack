package com.victorblasco.dto;

import java.util.UUID;

/**
 * Registro DTO de respuesta para la confirmación de registro de usuario.
 *
 * @param id identificador único UUID asignado al nuevo usuario
 * @param email correo electrónico registrado
 *
 * @author Victor Blasco
 */
public record RegisterResponse(
        UUID id,
        String email
) {}
