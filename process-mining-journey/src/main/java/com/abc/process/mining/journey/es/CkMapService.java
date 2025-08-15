package com.abc.process.mining.journey.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.MgetResponse;
import co.elastic.clients.elasticsearch.core.mget.MultiGetResponseItem;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.json.JsonData;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class CkMapService {

    private final ElasticsearchClient es;
    private final String index; // typically "ckmap"

    public CkMapService(ElasticsearchClient es, String index) {
        this.es = es;
        this.index = index;
    }

    /**
     * Bulk fetch: { ck -> journeyId } for provided CKs. Missing docs are omitted.
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
     * Try to claim a CK for the given target journey.
     * - If empty: set journeyId = target
     * - If already target: noop
     * - If set to a different journey: return false (caller can retry/merge)
     */
    public boolean claimCk(String ck, String targetJourneyId) throws IOException {
        // Field-safe script; works if doc exists or not
        String script =
                "def j = ctx._source.containsKey('journeyId') ? ctx._source.journeyId : null; " +
                        "if (j == null) { ctx._source.journeyId = params.j; } " +
                        "else if (j != params.j) { ctx.op = 'none'; } " +
                        "ctx._source.updatedAt = params.now;";

        es.update(u -> u
                        .index("ckmap")
                        .id(ck)
                        .script(s -> s.inline(i -> i
                                .lang("painless")
                                .source(script)
                                .params("j",   JsonData.of(targetJourneyId))
                                .params("now", JsonData.of(Instant.now().toString()))
                        ))
                        // IMPORTANT: run the script even when the doc doesn't exist
                        .scriptedUpsert(true)
                        .upsert(Map.of(
                                "ck", ck,
                                "journeyId", targetJourneyId,   // initial claim
                                "updatedAt", Instant.now().toString()
                        )),
                Map.class
        );

        // Confirm current value
        var get = es.get(g -> g.index("ckmap").id(ck), Map.class);
        if (!get.found()) return false;
        @SuppressWarnings("unchecked")
        Map<String, Object> src = (Map<String, Object>) get.source();
        Object jid = src.get("journeyId");
        return targetJourneyId.equals(jid);
    }

}

