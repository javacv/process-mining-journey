package com.abc.process.mining.journey.kafka;

import com.abc.process.mining.journey.es.CkMapService;
import com.abc.process.mining.journey.es.JourneyService;
import com.abc.process.mining.journey.es.RedirectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RawEventsConsumer}.
 */
@ExtendWith(MockitoExtension.class)
class RawEventsConsumerTest {

    private ObjectMapper mapper;
    private CkMapService ckMapService;
    private JourneyService journeyService;
    private RedirectService redirectService;
    private RawEventsConsumer consumer;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        ckMapService = mock(CkMapService.class);
        journeyService = mock(JourneyService.class);
        redirectService = mock(RedirectService.class);
        consumer = new RawEventsConsumer(mapper, ckMapService, journeyService, redirectService);
    }

    @Test
    void consume_createsNewJourney_whenNoExistingMapping() throws Exception {
        String eventId = "E1";
        String json = """
            {"eventId":"%s","activity":"Start","timestamp":"2025-01-01T00:00:00Z","correlationKeys":["CK1"]}
            """.formatted(eventId);

        when(ckMapService.mgetJourneyIds(anyList())).thenReturn(Map.of());
        when(ckMapService.claimCk(anyString(), anyString())).thenReturn(true);
        when(redirectService.resolve(anyString())).thenAnswer(inv -> inv.getArgument(0));

        consumer.consume(json);

        verify(ckMapService).claimCk(eq("CK1"), anyString());
        verify(journeyService).indexEvent(anyMap(), eq("events-" + LocalDate.now()));
        verify(journeyService).upsertJourney(anyString(), anyMap());
    }

    @Test
    void consume_usesExistingJourneyId() throws Exception {
        String json = """
            {"eventId":"E2","activity":"Check","timestamp":"2025-01-01T00:00:00Z","correlationKeys":["CK2"]}
            """;

        when(ckMapService.mgetJourneyIds(anyList())).thenReturn(Map.of("CK2", "J-123"));
        when(ckMapService.claimCk("CK2", "J-123")).thenReturn(true);
        when(redirectService.resolve(anyString())).thenAnswer(inv -> inv.getArgument(0));

        consumer.consume(json);

        verify(ckMapService).claimCk("CK2", "J-123");
        verify(journeyService).upsertJourney(eq("J-123"), anyMap());
    }

    @Test
    void consume_mergesJourneys_whenClaimFails() throws Exception {
        String json = """
            {"eventId":"E3","activity":"MergeTest","timestamp":"2025-01-01T00:00:00Z","correlationKeys":["CK3"]}
            """;

        // Pretend CK3 is already mapped to another journey
        when(ckMapService.mgetJourneyIds(anyList())).thenReturn(Map.of("CK3", "J-OLD"));
        when(ckMapService.claimCk("CK3", "J-NEW")).thenReturn(false);
        when(redirectService.resolve(anyString())).thenAnswer(inv -> inv.getArgument(0));

        // Capture redirect call
        doNothing().when(redirectService).setRedirect(anyString(), anyString());

        // Use UUID random to produce J-NEW
        try (var mocked = mockStatic(UUID.class)) {
            mocked.when(UUID::randomUUID).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000001"));

            consumer.consume(json);

            verify(redirectService).setRedirect(anyString(), anyString());
            verify(journeyService).upsertJourney(anyString(), anyMap());
        }
    }

    @Test
    void consume_throws_whenEventIdMissing() {
        String json = """
            {"activity":"X","timestamp":"2025-01-01T00:00:00Z","correlationKeys":["CKX"]}
            """;

        assertThrows(IllegalArgumentException.class, () -> consumer.consume(json));
        verifyNoInteractions(journeyService);
    }

    @Test
    void consume_throws_whenCorrelationKeysMissing() {
        String json = """
            {"eventId":"E4","activity":"Y","timestamp":"2025-01-01T00:00:00Z"}
            """;

        assertThrows(IllegalArgumentException.class, () -> consumer.consume(json));
        verifyNoInteractions(journeyService);
    }
}

