package com.victorblasco.fintrack.ingest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Punto de entrada principal para el microservicio de ingesta de datos bancarios.
 * <p>
 * Este servicio gestiona la recepción de transacciones en tiempo real vía Webhooks PSD2
 * e importaciones masivas de extractos bancarios en formato CSV.
 * </p>
 *
 * @author Victor Blasco
 * @version 0.0.1-SNAPSHOT
 */
@SpringBootApplication
@EnableAsync
public class BankingIngestApplication {

    /**
     * Arranca la aplicación de Spring Boot.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(BankingIngestApplication.class, args);
    }
}
