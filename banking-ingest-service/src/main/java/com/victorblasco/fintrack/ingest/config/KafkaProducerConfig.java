package com.victorblasco.fintrack.ingest.config;

import com.victorblasco.fintrack.ingest.dto.RawTransactionEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración del productor de Apache Kafka.
 * Define los beans de fábrica de productores y plantilla de Kafka (KafkaTemplate)
 * fuertemente tipados para eventos {@link RawTransactionEvent}.
 *
 * @author Victor Blasco
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /**
     * Define la fábrica de productores de Kafka.
     *
     * @return fábrica de productores configurada con serializadores de clave String y valor JSON
     */
    @Bean
    public ProducerFactory<String, RawTransactionEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Define la plantilla de Kafka para el envío de eventos.
     *
     * @return plantilla de Kafka tipada para {@link RawTransactionEvent}
     */
    @Bean
    public KafkaTemplate<String, RawTransactionEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
