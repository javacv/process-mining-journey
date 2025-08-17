package com.abc.process.mining.journey.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ElasticsearchConfig}.
 *
 * These tests validate that the configuration class creates a
 * usable {@link ElasticsearchClient} bean given injected property values.
 */
public class ElasticsearchConfigTest {

    private ElasticsearchConfig config;

    @BeforeEach
    void setUp() {
        config = new ElasticsearchConfig();

        // Use reflection or direct field access since values are injected via @Value in production
        setField(config, "esHost", "localhost");
        setField(config, "esPort", 9200);
        setField(config, "esScheme", "http");
    }

    @Test
    @DisplayName("elasticsearchClient(): returns a non-null client instance")
    void testElasticsearchClientNotNull() {
        ElasticsearchClient client = config.elasticsearchClient();
        assertNotNull(client, "ElasticsearchClient should not be null");
    }

    // ---- helper: set private field via reflection ----
    private void setField(Object target, String fieldName, Object value) {
        try {
            var f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }
}
