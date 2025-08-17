package com.abc.process.mining.journey.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

import java.io.IOException;
import java.util.Map;

/**
 * Service for managing journeys and events in Elasticsearch.
 * <p>
 * This service provides methods for indexing individual events and for upserting
 * journey documents based on incoming events. The upsert logic uses an Elasticsearch
 * painless script to efficiently update journey documents by tracking timestamps,
 * event counts, and collecting unique event IDs and correlation keys.
 * </p>
 */
public class JourneyService {

    private final ElasticsearchClient es;
    private final String journeysIndex;
    private final String script =
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
    /**
     * Constructs a {@code JourneyService}.
     *
     * @param es The Elasticsearch client.
     * @param journeysIndex The name of the index where journey documents are stored.
     */
    public JourneyService(ElasticsearchClient es, String journeysIndex) {
        this.es = es;
        this.journeysIndex = journeysIndex;
    }

    /**
     * Indexes a single event document into the specified events index.
     * <p>
     * The document's ID is derived from the "eventId" field within the provided map.
     * </p>
     * @param eventDoc The map representing the event document. It must contain an "eventId" key.
     * @param eventsIndex The name of the index where event documents are stored.
     * @throws IOException if an I/O error occurs during the Elasticsearch operation.
     * @throws IllegalArgumentException if the "eventId" field is missing from the eventDoc.
     */
    public void indexEvent(Map<String, Object> eventDoc, String eventsIndex) throws IOException {
        String id = (String) eventDoc.get("eventId");
        if (id == null) throw new IllegalArgumentException("eventDoc must contain eventId");
        es.index(i -> i.index(eventsIndex).id(id).document(eventDoc));
    }

    /**
     * Upserts a journey document by processing a new event.
     * <p>
     * The method uses a painless script to update an existing journey document or create a new one.
     * The script performs the following actions:
     * <ul>
     * <li>Updates the {@code firstSeenAt} timestamp if the new event is older.</li>
     * <li>Updates the {@code lastSeenAt} timestamp if the new event is newer.</li>
     * <li>Increments the event counter.</li>
     * <li>Adds new correlation keys and event IDs to their respective sets.</li>
     * <li>Adds the new event's details to the journey's timeline.</li>
     * </ul>
     * The {@code scriptedUpsert} option is used to ensure the script runs even if the journey document does not yet exist.
     * </p>
     * @param journeyId The unique identifier of the journey.
     * @param eventDoc The map representing the event document to be incorporated into the journey.
     * @throws IOException if an I/O error occurs during the Elasticsearch operation.
     */
    public void upsertJourney(String journeyId, Map<String, Object> eventDoc) throws IOException {
        String ts = (String) eventDoc.get("timestamp");
        long tsEpoch = java.time.Instant.parse(ts).toEpochMilli();
        es.update(u -> u
                        .index(journeysIndex)
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
                        .scriptedUpsert(true)
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
