package com.victorblasco.fintrack.profile.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad para el microservicio de perfil financiero y libro mayor.
 * <p>
 * Permite que la pasarela API Gateway (proxy.ts) gestione la autenticación Zero-Trust
 * e inyecte la identidad validada a través de la cabecera `X-User-Id`.
 * </p>
 *
 * @author Victor Blasco
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/events/**", "/api/v1/events/stream").permitAll()
                .anyRequest().permitAll()
            );
        return http.build();
    }
}
