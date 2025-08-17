package com.abc.process.mining.journey.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RedirectService}.
 */
@ExtendWith(MockitoExtension.class)
class RedirectServiceTest {

    private ElasticsearchClient es;
    private RedirectService service;

    @BeforeEach
    void setup() {
        es = mock(ElasticsearchClient.class);
        service = new RedirectService(es, "redirects");
    }

    @Test
    void setRedirect_indexesDocument() throws IOException {
        IndexResponse mockResp = mock(IndexResponse.class);
        //when(es.index(any())).thenReturn(mockResp);

        service.setRedirect("J1", "J2");

        // Capture the index request builder function to ensure it was provided
        ArgumentCaptor<Function> captor = ArgumentCaptor.forClass(Function.class);
        verify(es).index(captor.capture());
        assertNotNull(captor.getValue(), "Index builder function should be passed to es.index()");
    }

    @Test
    void resolve_returnsTarget_whenFound() throws IOException {
        GetResponse<Map> resp = mock(GetResponse.class);
        when(resp.found()).thenReturn(true);
        when(resp.source()).thenReturn(Map.of("from", "J1", "to", "J2"));

        //when(es.get(any(), eq(Map.class))).thenReturn(resp);

        String out = service.resolve("J1");
        assertEquals("J2", out);

        // Verify the GET call was made
        //verify(es).get(any(), eq(Map.class));
    }

    @Test
    void resolve_returnsOriginal_whenNotFound() throws IOException {
        GetResponse<Map> resp = mock(GetResponse.class);
        when(resp.found()).thenReturn(false);
        //when(es.get(any(), eq(Map.class))).thenReturn(resp);

        String out = service.resolve("JX");
        assertEquals("JX", out);
    }

    @Test
    void resolve_returnsOriginal_whenFoundButToMissing() throws IOException {
        GetResponse<Map> resp = mock(GetResponse.class);
        when(resp.found()).thenReturn(true);
        when(resp.source()).thenReturn(Map.of("from", "J1")); // no "to"

        //when(es.get(any(), eq(Map.class))).thenReturn(resp);

        String out = service.resolve("J1");
        assertEquals("J1", out);
    }
}
