package com.abc.process.mining.journey.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CorrelationKeyMapTest {

    @Test
    void noArgsConstructor_initializesWithNulls() {
        CorrelationKeyMap m = new CorrelationKeyMap();
        assertNull(m.getCk());
        assertNull(m.getJourneyId());
    }

    @Test
    void allArgsConstructor_setsFields() {
        CorrelationKeyMap m = new CorrelationKeyMap("CK1", "J1");
        assertEquals("CK1", m.getCk());
        assertEquals("J1", m.getJourneyId());
    }

    @Test
    void setters_updateFields() {
        CorrelationKeyMap m = new CorrelationKeyMap();
        m.setCk("CK2");
        m.setJourneyId("J2");
        assertEquals("CK2", m.getCk());
        assertEquals("J2", m.getJourneyId());
    }

    @Test
    void setters_acceptNulls() {
        CorrelationKeyMap m = new CorrelationKeyMap("CK3", "J3");
        m.setCk(null);
        m.setJourneyId(null);
        assertNull(m.getCk());
        assertNull(m.getJourneyId());
    }
}
