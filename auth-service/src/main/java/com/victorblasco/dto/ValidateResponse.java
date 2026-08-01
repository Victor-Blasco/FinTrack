package com.victorblasco.dto;

import java.util.UUID;

/**
 * Registro DTO para la respuesta de validación interna de tokens JWT.
 *
 * @param valid indica si el token proporcionado es auténtico y no ha expirado
 * @param userId identificador único del usuario (extraído del Subject de las claims del JWT)
 *
 * @author Victor Blasco
 */
public record ValidateResponse(
        boolean valid,
        UUID userId
) {}
