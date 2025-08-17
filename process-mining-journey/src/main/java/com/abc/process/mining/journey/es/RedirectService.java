package com.abc.process.mining.journey.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

import java.io.IOException;
import java.util.Map;

/**
 * Service for managing redirects in Elasticsearch.
 * <p>
 * This service provides functionality to set and resolve document redirects,
 * which is useful for merging journeys or other documents. It stores redirect
 * information in a dedicated index.
 * </p>
 */
public class RedirectService {

    private final ElasticsearchClient es;
    private final String index;

    /**
     * Constructs a {@code RedirectService}.
     *
     * @param es The Elasticsearch client.
     * @param index The name of the index where redirect documents are stored.
     */
    public RedirectService(ElasticsearchClient es, String index) {
        this.es = es;
        this.index = index;
    }

    /**
     * Sets a redirect from a source ID to a target ID.
     * <p>
     * This method indexes a document with the source ID, containing the target ID
     * and a timestamp of the update.
     * </p>
     * @param from The source ID to redirect from.
     * @param to The target ID to redirect to.
     * @throws IOException if an I/O error occurs during the Elasticsearch operation.
     */
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

    /**
     * Resolves the final destination ID for a given journey ID, following redirects.
     * <p>
     * It performs a get request on the redirects index. If a document is found,
     * it extracts the "to" field and returns it. If the document is not found,
     * it returns the original ID, implying no redirect exists.
     * </p>
     * @param journeyId The ID to resolve.
     * @return The final destination ID.
     * @throws IOException if an I/O error occurs during the Elasticsearch operation.
     */
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
