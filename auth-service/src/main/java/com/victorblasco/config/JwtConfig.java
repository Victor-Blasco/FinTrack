package com.victorblasco.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Configuración de seguridad para el cifrado, decodificación y firma de tokens JWT.
 * Utiliza algoritmos de firma simétrica HMAC-SHA256 respaldados por Nimbus JWT.
 *
 * @author Victor Blasco
 */
@Configuration
public class JwtConfig {

    @Value("${jwt.secret:defaultSecretKeyThatIsAtLeast32BytesLongForHmacSha256!}")
    private String jwtSecret;

    /**
     * Define la clave secreta HMAC-SHA256 utilizada para firmar y verificar tokens JWT.
     *
     * @return objeto {@link SecretKey} para cifrado simétrico
     */
    @Bean
    public SecretKey secretKey() {
        return new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    /**
     * Registra el codificador (firmador) de tokens JWT.
     *
     * @param secretKey clave secreta inyectada
     * @return instancia de {@link JwtEncoder}
     */
    @Bean
    public JwtEncoder jwtEncoder(SecretKey secretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    /**
     * Registra el decodificador (verificador) de tokens JWT.
     *
     * @param secretKey clave secreta inyectada
     * @return instancia de {@link JwtDecoder}
     */
    @Bean
    public JwtDecoder jwtDecoder(SecretKey secretKey) {
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }
}
