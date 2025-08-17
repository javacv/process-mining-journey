package com.abc.process.mining.journey.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration class for a Spring application.
 * <p>
 * This class uses {@code @EnableKafka} and {@code @Configuration} annotations to
 * set up the necessary beans for producing and consuming messages from a Kafka cluster.
 * It defines beans for the consumer factory, producer factory, a Kafka listener container factory,
 * and a {@link org.springframework.kafka.core.KafkaTemplate}.
 * </p>
 */
@EnableKafka
@Configuration
public class KafkaConfig {

    /**
     * Creates and configures a {@link org.springframework.kafka.core.ConsumerFactory} bean.
     * <p>
     * This factory is used to create Kafka consumer instances. It is configured with
     * a hardcoded bootstrap server address, a consumer group ID, and
     * {@link org.apache.kafka.common.serialization.StringDeserializer} for keys and values.
     * Automatic committing of offsets is disabled.
     * </p>
     * @return A {@link org.springframework.kafka.core.DefaultKafkaConsumerFactory} instance.
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "journey-consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Creates and configures a {@link org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory} bean.
     * <p>
     * This factory is used to create containers for Kafka listeners. It uses the
     * {@code consumerFactory()} bean to configure the consumers and is set up
     * to handle one message at a time ({@code setBatchListener(false)}).
     * </p>
     * @return A {@link org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory} instance.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setBatchListener(false);
        return factory;
    }

    /**
     * Creates and configures a {@link org.springframework.kafka.core.ProducerFactory} bean.
     * <p>
     * This factory is used to create Kafka producer instances. It is configured with
     * a hardcoded bootstrap server address and {@link org.apache.kafka.common.serialization.StringSerializer}
     * for both keys and values.
     * </p>
     * @return A {@link org.springframework.kafka.core.DefaultKafkaProducerFactory} instance.
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * Creates and configures a {@link org.springframework.kafka.core.KafkaTemplate} bean.
     * <p>
     * The template is a high-level component used to send messages to Kafka topics.
     * It is initialized with the {@code producerFactory()} bean.
     * </p>
     * @return A {@link org.springframework.kafka.core.KafkaTemplate} instance.
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}