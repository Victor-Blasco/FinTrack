package com.victorblasco.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Registro DTO para la solicitud de alta de nuevo usuario.
 *
 * @param email correo electrónico con el que se registrará el usuario
 * @param password contraseña deseada (mínimo 6 caracteres)
 *
 * @author Victor Blasco
 */
public record RegisterRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe ser válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password
) {}
