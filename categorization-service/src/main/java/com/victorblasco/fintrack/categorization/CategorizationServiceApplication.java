package com.victorblasco.fintrack.categorization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada principal para el microservicio de categorización y enriquecimiento de datos.
 */
@SpringBootApplication
public class CategorizationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CategorizationServiceApplication.class, args);
    }
}
