package com.victorblasco.fintrack.profile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Punto de entrada principal para el microservicio de perfil financiero y libro mayor (finance-profile-service).
 * <p>
 * Gestiona el almacenamiento de saldos de cuentas, historial de transacciones paginado,
 * presupuesto del usuario y consumo en tiempo real de eventos Kafka (raw-transactions, fraud-verdicts, categorized-events).
 * </p>
 *
 * @author Victor Blasco
 */
@SpringBootApplication
@EnableKafka
public class FinanceProfileServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceProfileServiceApplication.class, args);
    }
}
