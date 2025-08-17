package com.abc.process.mining.journey.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link KafkaConfig}.
 * Verifies bean wiring and core configuration without requiring a Kafka broker.
 */
public class KafkaConfigTest {

    @Test
    void consumerFactory_hasExpectedProperties() {
        KafkaConfig cfg = new KafkaConfig();
        ConsumerFactory<String, String> cf = cfg.consumerFactory();
        assertNotNull(cf);

        // DefaultKafkaConsumerFactory exposes its configuration map
        assertTrue(cf instanceof DefaultKafkaConsumerFactory);
        Map<String, Object> props = ((DefaultKafkaConsumerFactory<String, String>) cf).getConfigurationProperties();

        assertEquals("localhost:9092", props.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals("journey-consumer-group", props.get(ConsumerConfig.GROUP_ID_CONFIG));
        assertEquals(false, props.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG));
        assertEquals("org.apache.kafka.common.serialization.StringDeserializer",
                String.valueOf(props.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG)));
        assertEquals("org.apache.kafka.common.serialization.StringDeserializer",
                String.valueOf(props.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG)));
    }

    @Test
    void listenerContainerFactory_usesConsumerFactory_andIsNotBatch() {
        KafkaConfig cfg = new KafkaConfig();

        // Spy so we can ensure the listener factory uses the same ConsumerFactory
        KafkaConfig spyCfg = Mockito.spy(cfg);
        ConsumerFactory<String, String> mockCF = mock(ConsumerFactory.class);
        doReturn(mockCF).when(spyCfg).consumerFactory();

        ConcurrentKafkaListenerContainerFactory<String, String> lcf = spyCfg.kafkaListenerContainerFactory();
        assertNotNull(lcf);
        assertSame(mockCF, lcf.getConsumerFactory(), "Listener factory should use our ConsumerFactory");
        assertFalse(lcf.isBatchListener(), "Factory should be configured for single-message (non-batch) consumption");
    }

    @Test
    void producerFactory_hasExpectedProperties() {
        KafkaConfig cfg = new KafkaConfig();
        ProducerFactory<String, String> pf = cfg.producerFactory();
        assertNotNull(pf);

        assertTrue(pf instanceof DefaultKafkaProducerFactory);
        Map<String, Object> props = ((DefaultKafkaProducerFactory<String, String>) pf).getConfigurationProperties();

        assertEquals("localhost:9092", props.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals("org.apache.kafka.common.serialization.StringSerializer",
                String.valueOf(props.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)));
        assertEquals("org.apache.kafka.common.serialization.StringSerializer",
                String.valueOf(props.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)));
    }

    @Test
    void kafkaTemplate_isConstructedFromProducerFactory() {
        KafkaConfig cfg = new KafkaConfig();
        KafkaConfig spyCfg = Mockito.spy(cfg);

        @SuppressWarnings("unchecked")
        ProducerFactory<String, String> mockPF = mock(ProducerFactory.class);
        doReturn(mockPF).when(spyCfg).producerFactory();

        KafkaTemplate<String, String> template = spyCfg.kafkaTemplate();
        assertNotNull(template);
        assertSame(mockPF, template.getProducerFactory(), "KafkaTemplate should be created from the ProducerFactory");
    }
}
