package com.abc.process.mining.journey.config;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ElasticsearchConfig {

    @Value("${app.elastic.host}") private String esHost;
    @Value("${app.elastic.port}") private int esPort;
    @Value("${app.elastic.scheme:http}") private String esScheme;

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
