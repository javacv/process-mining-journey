package com.abc.process.mining.journey.kafka;

import com.abc.process.mining.journey.es.CkMapService;
import com.abc.process.mining.journey.es.JourneyService;
import com.abc.process.mining.journey.es.RedirectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class RawEventsConsumer {

    private final ObjectMapper mapper;
    private final CkMapService ckMapService;
    private final JourneyService journeyService;
    private final RedirectService redirectService;

    public RawEventsConsumer(ObjectMapper mapper,
                             CkMapService ckMapService,
                             JourneyService journeyService,
                             RedirectService redirectService) {
        this.mapper = mapper;
        this.ckMapService = ckMapService;
        this.journeyService = journeyService;
        this.redirectService = redirectService;
    }

    @KafkaListener(topics = "events.raw", groupId = "journey-consumer-group")
    public void consume(String message) throws IOException {
        System.out.println("********** "+message+" **************");
        // Parse raw JSON into Map
        @SuppressWarnings("unchecked")
        Map<String, Object> eventDoc = mapper.readValue(message, Map.class);

        String eventId = (String) eventDoc.get("eventId");
        if (eventId == null) {
            throw new IllegalArgumentException("eventId missing in event: " + message);
        }

        @SuppressWarnings("unchecked")
        List<String> cks = (List<String>) eventDoc.get("correlationKeys");
        if (cks == null || cks.isEmpty()) {
            throw new IllegalArgumentException("No correlation keys for event: " + eventId);
        }

        // Step 1: Check if any CK is already mapped to a journey
        Map<String, String> existing = ckMapService.mgetJourneyIds(cks);

        String journeyId;
        if (!existing.isEmpty()) {
            // Pick first found journeyId
            journeyId = existing.values().iterator().next();
        } else {
            // Generate a new journey ID
            journeyId = UUID.randomUUID().toString();
        }

        // Step 2: Claim each CK for this journey
        for (String ck : cks) {
            boolean claimed = ckMapService.claimCk(ck, journeyId);
            if (!claimed) {
                // Merge required — different journeyId already claimed this CK
                String otherJourney = existing.get(ck);
                if (otherJourney != null && !otherJourney.equals(journeyId)) {
                    // Merge: redirect smaller ID → larger ID for consistency
                    String target = journeyId.compareTo(otherJourney) < 0 ? journeyId : otherJourney;
                    String source = journeyId.equals(target) ? otherJourney : journeyId;
                    redirectService.setRedirect(source, target);
                    journeyId = target;
                }
            }
        }

        // Step 3: Resolve any redirects (1 hop)
        journeyId = redirectService.resolve(journeyId);

        // Step 4: Store event and upsert journey
        journeyService.indexEvent(eventDoc, "events-" + java.time.LocalDate.now());
        journeyService.upsertJourney(journeyId, eventDoc);
    }
}

