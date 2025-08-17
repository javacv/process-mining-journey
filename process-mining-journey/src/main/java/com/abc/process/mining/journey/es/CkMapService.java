package com.abc.process.mining.journey.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.MgetResponse;
import co.elastic.clients.elasticsearch.core.mget.MultiGetResponseItem;
import co.elastic.clients.json.JsonData;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
/**
 * Service for managing the mapping from correlation keys to journey IDs in Elasticsearch.
 * <p>
 * This service provides methods for efficiently retrieving journey IDs for a list of
 * correlation keys and for atomically claiming a correlation key for a specific journey,
 * which is a critical operation for managing user sessions or journeys.
 * </p>
 */
public class CkMapService {

    private final ElasticsearchClient es;
    private final String index; // typically "ckmap"

    /**
     * Constructs a {@code CkMapService}.
     *
     * @param es The Elasticsearch client.
     * @param index The name of the index where the CK-to-journey ID mapping documents are stored.
     */
    public CkMapService(ElasticsearchClient es, String index) {
        this.es = es;
        this.index = index;
    }

    /**
     * Bulk fetches journey IDs for a given collection of correlation keys.
     * <p>
     * This method uses Elasticsearch's Multi-Get API to efficiently retrieve the documents
     * for all provided keys. It returns a map containing only the keys that were found
     * and their corresponding journey IDs. Missing documents are omitted from the result.
     * </p>
     *
     * @param cks A collection of correlation keys.
     * @return A map where the key is the correlation key and the value is the journey ID.
     * @throws IOException if an I/O error occurs during the Elasticsearch operation.
     */
    public Map<String, String> mgetJourneyIds(Collection<String> cks) throws IOException {
        if (cks == null || cks.isEmpty()) return Collections.emptyMap();
        MgetResponse<Map> resp = es.mget(m -> m.index(index).ids(new ArrayList<>(cks)), Map.class);

        Map<String, String> out = new HashMap<>();
        for (MultiGetResponseItem<Map> item : resp.docs()) {
            if (item.result().found()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> src = (Map<String, Object>) item.result().source();
                Object jid = src.get("journeyId");
                if (jid != null) {
                    out.put(item.result().id(), jid.toString());
                }
            }
        }
        return out;
    }

    /**
     * Attempts to claim a correlation key for a specific target journey ID.
     * <p>
     * This method uses an Elasticsearch painless script to perform an atomic update operation.
     * The script logic is as follows:
     * <ul>
     * <li>If the document does not exist or has no {@code journeyId}, it sets the {@code journeyId} to the target.</li>
     * <li>If the {@code journeyId} is already the target, it performs no action.</li>
     * <li>If the {@code journeyId} is different from the target, the script does nothing, effectively
     * preventing the claim.</li>
     * </ul>
     * The method then verifies the claim by performing a subsequent GET request and returns the result.
     * </p>
     *
     * @param ck The correlation key to claim.
     * @param targetJourneyId The journey ID to associate with the correlation key.
     * @return {@code true} if the claim was successful, {@code false} otherwise.
     * @throws IOException if an I/O error occurs during the Elasticsearch operation.
     */
    public boolean claimCk(String ck, String targetJourneyId) throws IOException {
        String script =
                "def j = ctx._source.containsKey('journeyId') ? ctx._source.journeyId : null; " +
                        "if (j == null) { ctx._source.journeyId = params.j; } " +
                        "else if (j != params.j) { ctx.op = 'none'; } " +
                        "ctx._source.updatedAt = params.now;";

        es.update(u -> u
                        .index(index)
                        .id(ck)
                        .script(s -> s.inline(i -> i
                                .lang("painless")
                                .source(script)
                                .params("j",   JsonData.of(targetJourneyId))
                                .params("now", JsonData.of(Instant.now().toString()))
                        ))
                        .scriptedUpsert(true)
                        .upsert(Map.of(
                                "ck", ck,
                                "journeyId", targetJourneyId,
                                "updatedAt", Instant.now().toString()
                        )),
                Map.class
        );

        var get = es.get(g -> g.index(index).id(ck), Map.class);
        if (!get.found()) return false;
        @SuppressWarnings("unchecked")
        Map<String, Object> src = (Map<String, Object>) get.source();
        Object jid = src.get("journeyId");
        return targetJourneyId.equals(jid);
    }
}

