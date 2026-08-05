package com.victorblasco.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de configuración tipadas para el servicio de autenticación JWT.
 *
 * @param secret clave secreta de firma HMAC-SHA256
 * @param expirationHours tiempo de validez del token en horas
 * @param issuer nombre del emisor del token
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        long expirationHours,
        String issuer
) {
}
