package com.abc.process.mining.journey.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

import java.io.IOException;
import java.util.Map;

public class RedirectService {

    private final ElasticsearchClient es;
    private final String index; // e.g., "redirects"

    public RedirectService(ElasticsearchClient es, String index) {
        this.es = es;
        this.index = index;
    }

    public void setRedirect(String from, String to) throws IOException {
        es.index(i -> i
                .index(index)
                .id(from)
                .document(Map.of(
                        "from", from,
                        "to", to,
                        "updatedAt", java.time.Instant.now().toString()
                )));
    }

    public String resolve(String journeyId) throws IOException {
        var resp = es.get(g -> g.index(index).id(journeyId), Map.class);
        if (resp.found()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> src = (Map<String, Object>) resp.source();
            Object to = src.get("to");
            if (to != null) return to.toString();
        }
        return journeyId;
    }
}
