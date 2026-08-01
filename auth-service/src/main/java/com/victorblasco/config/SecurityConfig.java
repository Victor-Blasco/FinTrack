package com.victorblasco.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de la cadena de filtros de Spring Security para auth-service.
 * <p>
 * Deshabilita CSRF, establece una política de sesión sin estado (STATELESS)
 * y configura el codificador de contraseñas {@link BCryptPasswordEncoder}.
 * </p>
 *
 * @author Victor Blasco
 */
@Configuration
public class SecurityConfig {

    /**
     * Registra el codificador de contraseñas BCrypt para almacenar passwords de forma segura.
     *
     * @return instancia de {@link PasswordEncoder} basada en BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura las reglas de acceso HTTP y deshabilita sesiones de servidor.
     *
     * @param http objeto de configuración de Spring Security
     * @return cadena de filtros de seguridad compilada
     * @throws Exception en caso de error de configuración de seguridad
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
