package com.abc.process.mining.journey;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.abc.process.mining.journey.es.CkMapService;
import com.abc.process.mining.journey.es.JourneyService;
import com.abc.process.mining.journey.es.RedirectService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JourneyStitchApplication {
    public static void main(String[] args) {
        SpringApplication.run(JourneyStitchApplication.class, args);
    }

    @Bean
    public CkMapService ckMapService(ElasticsearchClient es) {
        return new CkMapService(es, "ckmap");
    }

    @Bean
    public JourneyService journeyService(ElasticsearchClient es) {
        return new JourneyService(es, "journeys-v1");
    }

    @Bean
    public RedirectService redirectService(ElasticsearchClient es) {
        return new RedirectService(es, "redirects");
    }
}
