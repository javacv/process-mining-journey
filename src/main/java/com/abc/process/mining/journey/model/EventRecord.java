package com.abc.process.mining.journey.model;

import java.time.Instant;
import java.util.List;

public class EventRecord {

    private String eventId;
    private String activity;
    private List<String> correlationKeys;
    private Instant timestamp;

    public EventRecord() {
    }

    public EventRecord(String eventId, String activity, List<String> correlationKeys, Instant timestamp) {
        this.eventId = eventId;
        this.activity = activity;
        this.correlationKeys = correlationKeys;
        this.timestamp = timestamp;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public List<String> getCorrelationKeys() {
        return correlationKeys;
    }

    public void setCorrelationKeys(List<String> correlationKeys) {
        this.correlationKeys = correlationKeys;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
