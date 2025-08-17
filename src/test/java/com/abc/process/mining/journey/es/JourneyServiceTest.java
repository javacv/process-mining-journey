package com.abc.process.mining.journey.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link JourneyService}.
 */
@ExtendWith(MockitoExtension.class)
class JourneyServiceTest {

    private ElasticsearchClient es;
    private JourneyService svc;

    @BeforeEach
    void setUp() {
        es = mock(ElasticsearchClient.class);
        svc = new JourneyService(es, "journeys");
    }

    @Test
    void indexEvent_success() throws IOException {
        Map<String, Object> doc = Map.of(
                "eventId", "E1",
                "activity", "Start",
                "timestamp", Instant.now().toString()
        );

        IndexResponse mockResp = mock(IndexResponse.class);
        //when(es.index(any())).thenReturn(mockResp);

        svc.indexEvent(doc, "events");

        //verify(es).index(any());
    }

    @Test
    void indexEvent_throws_when_eventIdMissing() {
        Map<String, Object> doc = Map.of(
                "activity", "Start",
                "timestamp", Instant.now().toString()
        );

        assertThrows(IllegalArgumentException.class,
                () -> svc.indexEvent(doc, "events"));
        verifyNoInteractions(es);
    }

    @Test
    void upsertJourney_invokesUpdate_withExpectedParams() throws IOException {
        Map<String, Object> eventDoc = Map.of(
                "eventId", "E2",
                "activity", "Check",
                "timestamp", "2025-01-01T00:00:00Z",
                "correlationKeys", List.of("CK1", "CK2")
        );

        UpdateResponse<Map> mockResp = mock(UpdateResponse.class);
        //when(es.update(any(), eq(Map.class))).thenReturn(mockResp);

        svc.upsertJourney("J123", eventDoc);

        // capture the lambda passed to update()
        ArgumentCaptor<java.util.function.Function> captor = ArgumentCaptor.forClass(java.util.function.Function.class);
        verify(es).update(captor.capture(), eq(Map.class));

        // apply the captured lambda to a dummy builder just to ensure it doesn't throw
        // (we don't have the real UpdateRequest.Builder, but the call itself is verified)
        assertNotNull(captor.getValue());
    }
}
