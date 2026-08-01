package com.victorblasco;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada principal para el microservicio de autenticación y seguridad (auth-service).
 * <p>
 * Gestiona el registro de usuarios, la validación de credenciales con cifrado BCrypt
 * y la emisión/validación de tokens JWT para todo el ecosistema FinTrack.
 * </p>
 *
 * @author Victor Blasco
 * @version 0.0.1-SNAPSHOT
 */
@SpringBootApplication
public class AuthServiceApplication {

    /**
     * Arranca la aplicación Spring Boot para el servicio de autenticación.
     *
     * @param args argumentos de la línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

}
