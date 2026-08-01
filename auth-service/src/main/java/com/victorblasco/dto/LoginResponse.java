package com.victorblasco.dto;

import java.util.UUID;

/**
 * Registro DTO para la respuesta de inicio de sesión exitoso.
 *
 * @param token token JWT firmado para autenticar futuras peticiones
 * @param userId identificador único del usuario autenticado
 * @param email correo electrónico del usuario
 *
 * @author Victor Blasco
 */
public record LoginResponse(
        String token,
        UUID userId,
        String email
) {}
