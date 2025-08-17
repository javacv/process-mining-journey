package com.abc.process.mining.journey.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventRecordTest {

    @Test
    void noArgsConstructor_initializesWithNulls() {
        EventRecord record = new EventRecord();
        assertNull(record.getEventId());
        assertNull(record.getActivity());
        assertNull(record.getCorrelationKeys());
        assertNull(record.getTimestamp());
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        Instant ts = Instant.parse("2025-01-01T00:00:00Z");
        List<String> cks = List.of("CK1", "CK2");

        EventRecord record = new EventRecord("E1", "Start", cks, ts);

        assertEquals("E1", record.getEventId());
        assertEquals("Start", record.getActivity());
        assertEquals(cks, record.getCorrelationKeys());
        assertEquals(ts, record.getTimestamp());
    }

    @Test
    void setters_updateFields() {
        EventRecord record = new EventRecord();

        Instant ts = Instant.parse("2025-01-02T12:00:00Z");
        List<String> cks = List.of("CKX");

        record.setEventId("E2");
        record.setActivity("Check");
        record.setCorrelationKeys(cks);
        record.setTimestamp(ts);

        assertEquals("E2", record.getEventId());
        assertEquals("Check", record.getActivity());
        assertEquals(cks, record.getCorrelationKeys());
        assertEquals(ts, record.getTimestamp());
    }

    @Test
    void setters_acceptNulls() {
        EventRecord record = new EventRecord("E3", "Approve", List.of("CKZ"), Instant.now());

        record.setEventId(null);
        record.setActivity(null);
        record.setCorrelationKeys(null);
        record.setTimestamp(null);

        assertNull(record.getEventId());
        assertNull(record.getActivity());
        assertNull(record.getCorrelationKeys());
        assertNull(record.getTimestamp());
    }
}
