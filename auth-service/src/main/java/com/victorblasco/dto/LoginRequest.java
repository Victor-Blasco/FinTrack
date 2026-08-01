package com.victorblasco.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Registro DTO para la solicitud de inicio de sesión.
 *
 * @param email dirección de correo electrónico del usuario
 * @param password contraseña en texto plano para verificar
 *
 * @author Victor Blasco
 */
public record LoginRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe ser válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password
) {}
