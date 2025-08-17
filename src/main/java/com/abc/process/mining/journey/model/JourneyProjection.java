package com.abc.process.mining.journey.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class JourneyProjection {

    private String journeyId;
    private List<String> cks;
    private List<String> eventIds;
    private Instant firstSeenAt;
    private Instant lastSeenAt;
    private String status;
    private Map<String, Long> counters;

    public JourneyProjection() {
    }

    public JourneyProjection(String journeyId, List<String> cks, List<String> eventIds,
                             Instant firstSeenAt, Instant lastSeenAt,
                             String status, Map<String, Long> counters) {
        this.journeyId = journeyId;
        this.cks = cks;
        this.eventIds = eventIds;
        this.firstSeenAt = firstSeenAt;
        this.lastSeenAt = lastSeenAt;
        this.status = status;
        this.counters = counters;
    }

    public String getJourneyId() {
        return journeyId;
    }

    public void setJourneyId(String journeyId) {
        this.journeyId = journeyId;
    }

    public List<String> getCks() {
        return cks;
    }

    public void setCks(List<String> cks) {
        this.cks = cks;
    }

    public List<String> getEventIds() {
        return eventIds;
    }

    public void setEventIds(List<String> eventIds) {
        this.eventIds = eventIds;
    }

    public Instant getFirstSeenAt() {
        return firstSeenAt;
    }

    public void setFirstSeenAt(Instant firstSeenAt) {
        this.firstSeenAt = firstSeenAt;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Long> getCounters() {
        return counters;
    }

    public void setCounters(Map<String, Long> counters) {
        this.counters = counters;
    }
}
