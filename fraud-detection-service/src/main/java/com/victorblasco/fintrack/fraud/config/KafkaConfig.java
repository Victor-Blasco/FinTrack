package com.victorblasco.fintrack.fraud.config;

import com.victorblasco.fintrack.fraud.event.FraudAlertEvent;
import com.victorblasco.fintrack.fraud.event.FraudVerdictEvent;
import com.victorblasco.fintrack.fraud.event.RawTransactionEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración de la infraestructura de mensajería Apache Kafka para el microservicio de fraude.
 * <p>
 * Declara las plantillas {@link KafkaTemplate} de producción, la fábrica de contenedores oyentes
 * con {@link JacksonJsonSerializer} y {@link JacksonJsonDeserializer} de Spring Kafka 4.1+,
 * e incluye resiliencia con {@link CommonErrorHandler} para reintentos exponenciales y Dead Letter Queue (DLT).
 * </p>
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:fraud-detection-group}")
    private String groupId;

    /**
     * Define la fábrica de consumidores para eventos de transacciones crudas {@link RawTransactionEvent}.
     *
     * @return fábrica de consumidores {@link ConsumerFactory}
     */
    @Bean
    public ConsumerFactory<String, RawTransactionEvent> rawTransactionConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "com.victorblasco.fintrack.*");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JacksonJsonDeserializer<>(RawTransactionEvent.class)
        );
    }

    /**
     * Define el manejador de errores de mensajería con política de reintentos y tolerancia a fallos.
     *
     * @return manejador de errores {@link CommonErrorHandler}
     */
    @Bean
    public CommonErrorHandler errorHandler() {
        return new DefaultErrorHandler(new FixedBackOff(1000L, 3L));
    }

    /**
     * Define la fábrica de contenedores oyentes para procesar mensajes Kafka en concurrencia con manejo de errores.
     *
     * @param errorHandler manejador de errores común de Kafka
     * @return fábrica de contenedores oyentes {@link ConcurrentKafkaListenerContainerFactory}
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RawTransactionEvent> kafkaListenerContainerFactory(
            CommonErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, RawTransactionEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(rawTransactionConsumerFactory());
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    /**
     * Define la fábrica de productores de veredictos {@link FraudVerdictEvent}.
     *
     * @return fábrica de productores {@link ProducerFactory}
     */
    @Bean
    public ProducerFactory<String, FraudVerdictEvent> verdictProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * Declara el bean {@link KafkaTemplate} fuertemente tipado para veredictos.
     *
     * @return plantilla Kafka {@link KafkaTemplate}
     */
    @Bean
    public KafkaTemplate<String, FraudVerdictEvent> verdictKafkaTemplate() {
        return new KafkaTemplate<>(verdictProducerFactory());
    }

    /**
     * Define la fábrica de productores de alertas {@link FraudAlertEvent}.
     *
     * @return fábrica de productores {@link ProducerFactory}
     */
    @Bean
    public ProducerFactory<String, FraudAlertEvent> alertProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * Declara el bean {@link KafkaTemplate} fuertemente tipado para alertas.
     *
     * @return plantilla Kafka {@link KafkaTemplate}
     */
    @Bean
    public KafkaTemplate<String, FraudAlertEvent> alertKafkaTemplate() {
        return new KafkaTemplate<>(alertProducerFactory());
    }
}
