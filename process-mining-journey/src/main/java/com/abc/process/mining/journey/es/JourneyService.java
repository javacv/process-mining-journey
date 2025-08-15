package com.abc.process.mining.journey.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.json.JsonData;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JourneyService {

    private final ElasticsearchClient es;
    private final String journeysIndex; // e.g., "journeys-v1"

    public JourneyService(ElasticsearchClient es, String journeysIndex) {
        this.es = es;
        this.journeysIndex = journeysIndex;
    }

    public void indexEvent(Map<String, Object> eventDoc, String eventsIndex) throws IOException {
        String id = (String) eventDoc.get("eventId");
        if (id == null) throw new IllegalArgumentException("eventDoc must contain eventId");
        es.index(i -> i.index(eventsIndex).id(id).document(eventDoc));
    }

    public void upsertJourney(String journeyId, Map<String, Object> eventDoc) throws IOException {
        String ts = (String) eventDoc.get("timestamp");                // e.g., "2025-08-15T10:00:00Z"
        long tsEpoch = java.time.Instant.parse(ts).toEpochMilli();      // numeric for comparisons

        String script =
                // firstSeenAt: keep the earliest
                "def curFirst = (ctx._source.containsKey('firstSeenAt') && ctx._source.firstSeenAt != null) " +
                        "  ? java.time.Instant.parse(ctx._source.firstSeenAt).toEpochMilli() : null; " +
                        "if (curFirst == null || params.tsEpoch < curFirst) { ctx._source.firstSeenAt = params.ts; } " +
                        // lastSeenAt: keep the latest
                        "def curLast = (ctx._source.containsKey('lastSeenAt') && ctx._source.lastSeenAt != null) " +
                        "  ? java.time.Instant.parse(ctx._source.lastSeenAt).toEpochMilli() : null; " +
                        "if (curLast == null || params.tsEpoch > curLast) { ctx._source.lastSeenAt = params.ts; } " +
                        // counters
                        "if (ctx._source.counters == null) { ctx._source.counters = ['events':0L,'distinctActivities':0L]; } " +
                        "ctx._source.counters.events += 1; " +
                        // sets (use java.util.* to avoid class resolution issues)
                        "if (ctx._source.cks == null) { ctx._source.cks = new java.util.HashSet(); } " +
                        "ctx._source.cks.addAll(params.cks); " +
                        "if (ctx._source.eventIds == null) { ctx._source.eventIds = new java.util.HashSet(); } " +
                        "ctx._source.eventIds.add(params.eid); " +
                        "if (ctx._source.timeline == null) { ctx._source.timeline = new java.util.ArrayList(); } " +
                        "ctx._source.timeline.add(['eventId':params.eid,'activity':params.act,'timestamp':params.ts]);";

        es.update(u -> u
                        .index("journeys-v1")
                        .id(journeyId)
                        .script(s -> s.inline(i -> i
                                .lang("painless")
                                .source(script)
                                .params("eid",   co.elastic.clients.json.JsonData.of((String) eventDoc.get("eventId")))
                                .params("act",   co.elastic.clients.json.JsonData.of((String) eventDoc.get("activity")))
                                .params("ts",    co.elastic.clients.json.JsonData.of(ts))
                                .params("tsEpoch", co.elastic.clients.json.JsonData.of(tsEpoch))
                                .params("cks",   co.elastic.clients.json.JsonData.of((java.util.List<String>) eventDoc.get("correlationKeys")))
                        ))
                        .scriptedUpsert(true)  // safe if doc doesn't exist
                        .upsert(java.util.Map.of(
                                "journeyId", journeyId,
                                "firstSeenAt", ts,
                                "lastSeenAt",  ts,
                                "status", "OPEN",
                                "cks", java.util.List.of(),
                                "eventIds", java.util.List.of(),
                                "counters", java.util.Map.of("events", 0, "distinctActivities", 0)
                        )),
                java.util.Map.class
        );
    }

}
