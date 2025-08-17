package com.abc.process.mining.journey.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Configuration class for creating and managing an Elasticsearch client.
 * <p>
 * This class uses Spring's {@code @Configuration} annotation to define beans
 * that are used to connect to and interact with an Elasticsearch cluster.
 * The host, port, and scheme for the Elasticsearch connection are loaded from
 * application properties.
 * </p>
 */
@Configuration
public class ElasticsearchConfig {

    /**
     * The host address of the Elasticsearch instance.
     * This value is injected from the application properties key {@code app.elastic.host}.
     */
    @Value("${app.elastic.host}")
    private String esHost;

    /**
     * The port number for the Elasticsearch instance.
     * This value is injected from the application properties key {@code app.elastic.port}.
     */
    @Value("${app.elastic.port}")
    private int esPort;

    /**
     * The scheme (e.g., http or https) to use for the Elasticsearch connection.
     * This value is injected from the application properties key {@code app.elastic.scheme}.
     * The default value is "http".
     */
    @Value("${app.elastic.scheme:http}")
    private String esScheme;

    /**
     * Creates and configures an {@link co.elastic.clients.elasticsearch.ElasticsearchClient} bean.
     * <p>
     * This method builds a {@link org.elasticsearch.client.RestClient} using the configured
     * host, port, and scheme. It then initializes an {@link co.elastic.clients.transport.ElasticsearchTransport}
     * with the RestClient and a JSON mapper, which is used to create the final ElasticsearchClient.
     * </p>
     * @return A fully configured {@link co.elastic.clients.elasticsearch.ElasticsearchClient} instance.
     */
    @Bean
    public ElasticsearchClient elasticsearchClient() {
        RestClient restClient = RestClient.builder(
                new HttpHost(esHost, esPort, esScheme)
        ).build();

        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new co.elastic.clients.json.jackson.JacksonJsonpMapper()
        );

        return new ElasticsearchClient(transport);
    }
}