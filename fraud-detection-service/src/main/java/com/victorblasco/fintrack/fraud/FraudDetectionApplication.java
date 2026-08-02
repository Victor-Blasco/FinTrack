package com.victorblasco.fintrack.fraud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada principal para la aplicación Spring Boot {@code fraud-detection-service}.
 * <p>
 * Este microservicio realiza el análisis de seguridad y detección de fraude en tiempo real
 * consumiendo el topic Kafka {@code raw-transactions} e inyectando veredictos y alertas.
 * </p>
 */
@SpringBootApplication
public class FraudDetectionApplication {

    /**
     * Método principal que arranca la aplicación Spring Boot.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(FraudDetectionApplication.class, args);
    }
}
