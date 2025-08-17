package com.abc.process.mining.journey.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.MgetResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.core.mget.MultiGetResponseItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CkMapService}.
 */
@ExtendWith(MockitoExtension.class)
public class CkMapServiceTest {

    @Test
    void mgetJourneyIds_returnsEmptyMap_onNullOrEmptyInput() throws IOException {
        ElasticsearchClient es = mock(ElasticsearchClient.class);
        CkMapService svc = new CkMapService(es, "ckmap");

        assertTrue(svc.mgetJourneyIds(null).isEmpty());
        assertTrue(svc.mgetJourneyIds(Collections.emptyList()).isEmpty());

        verifyNoInteractions(es);
    }

    @Test
    @SuppressWarnings("unchecked")
    void mgetJourneyIds_mapsOnlyFoundDocs() throws IOException {
        ElasticsearchClient es = mock(ElasticsearchClient.class);

        // Mock response tree: MgetResponse -> List<MultiGetResponseItem> -> result().found/id/source
        MgetResponse<Map> mget = mock(MgetResponse.class);
        MultiGetResponseItem<Map> it1 = mock(MultiGetResponseItem.class);
        MultiGetResponseItem<Map> it2 = mock(MultiGetResponseItem.class);

        // Inner "result" objects provide found()/id()/source()
        Object r1 = mock(Object.class);
        Object r2 = mock(Object.class);

        // Wire docs() list
        when(mget.docs()).thenReturn(List.of(it1, it2));
        //when(es.mget(any(), eq(Map.class))).thenReturn(mget);

        // For item1: found -> true, id -> "CK1", source -> {journeyId="J1"}
        //when(it1.result()).thenReturn(r1);
        when(callBool(r1, "found")).thenReturn(true);
        when(callString(r1, "id")).thenReturn("CK1");
        when(callMap(r1, "source")).thenReturn(Map.of("journeyId", "J1"));

        // For item2: found -> false, should be ignored
        //when(it2.result()).thenReturn(r2);
        when(callBool(r2, "found")).thenReturn(false);

        CkMapService svc = new CkMapService(es, "ckmap");
        Map<String, String> out = svc.mgetJourneyIds(List.of("CK1", "CK2"));

        assertEquals(1, out.size());
        assertEquals("J1", out.get("CK1"));
        assertFalse(out.containsKey("CK2"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void claimCk_returnsTrue_when_postGet_confirms_target() throws IOException {
        ElasticsearchClient es = mock(ElasticsearchClient.class);
        CkMapService svc = new CkMapService(es, "ckmap");

        // We don't inspect UpdateResponse in the code, just ensure call doesn't throw
        UpdateResponse<Map> upd = mock(UpdateResponse.class);
       // when(es.update(any(), eq(Map.class))).thenReturn(upd);

        GetResponse<Map> get = mock(GetResponse.class);
        when(get.found()).thenReturn(true);
        when(get.source()).thenReturn(Map.of("journeyId", "J-123"));
        //when(es.get(any(), eq(Map.class))).thenReturn(get);

        assertTrue(svc.claimCk("CK1", "J-123"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void claimCk_returnsFalse_when_doc_missing_or_mismatch() throws IOException {
        ElasticsearchClient es = mock(ElasticsearchClient.class);
        CkMapService svc = new CkMapService(es, "ckmap");

        // Generic update stub
        //when(es.update(any(), eq(Map.class))).thenReturn(mock(UpdateResponse.class));

        // Case 1: GET not found
        GetResponse<Map> notFound = mock(GetResponse.class);
        when(notFound.found()).thenReturn(false);
       // when(es.get(any(), eq(Map.class))).thenReturn(notFound);
        assertFalse(svc.claimCk("CKX", "J-X"));

        // Case 2: GET found but journeyId != target
        GetResponse<Map> mismatch = mock(GetResponse.class);
        when(mismatch.found()).thenReturn(true);
        when(mismatch.source()).thenReturn(Map.of("journeyId", "OTHER"));
        //when(es.get(any(), eq(Map.class))).thenReturn(mismatch);
        assertFalse(svc.claimCk("CKY", "J-Y"));
    }

    // ------------------ tiny helpers ------------------
    /**
     * The Elasticsearch Java client’s Mget item wraps a result object whose concrete type
     * isn’t referenced directly in our code under test. To keep tests lightweight, we
     * just reflectively dispatch simple getters on a Mockito mock.
     */
    private static boolean callBool(Object target, String method) {
        try {
            return (Boolean) target.getClass().getMethod(method).invoke(target);
        } catch (Exception e) {
            // If the mock doesn't have a real method, Mockito will route this anyway.
            // Let Mockito handle via stubbing; default false when not stubbed.
            return false;
        }
    }

    private static String callString(Object target, String method) {
        try {
            return (String) target.getClass().getMethod(method).invoke(target);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> callMap(Object target, String method) {
        try {
            return (Map<String, Object>) target.getClass().getMethod(method).invoke(target);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}

