package com.abc.process.mining.journey;


import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class JourneyEventGenerator {

    // --------- Configuration ---------
    static int N = 2; // journeys (j)
    static int M = 3; // applications per journey (a)
    static int L = 2; // events per application (p)
    static long stepSeconds = 1; // time increment per event in seconds
    // ----------------------------------

    public static void main(String[] args) {
        // Optional CLI: N M L [stepSeconds]
        if (args.length >= 3) {
            N = Integer.parseInt(args[0]);
            M = Integer.parseInt(args[1]);
            L = Integer.parseInt(args[2]);
        }
        if (args.length >= 4) {
            stepSeconds = Long.parseLong(args[3]);
        }

        DateTimeFormatter iso = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);
        Instant baseTime = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);

        long eventCounter = 0; // increments for each event to keep timestamp unique

        for (int j = 1; j <= N; j++) {             // journeys
            for (int a = 1; a <= M; a++) {         // applications
                for (int p = 1; p <= L; p++) {     // events per application
                    String eventId = "E" + a + p;                  // E{a}{p}
                    String activity = "Credit Check " + a;          // Credit Check {a}
                    String ck1 = "CK" + j + "" + a;                  // CK{j}{a}
                    String ck2 = "CK" + j + "" + (a + 1);            // CK{j}{a+1}

                    Instant eventTime = baseTime.plusSeconds(eventCounter * stepSeconds);
                    String timestamp = iso.format(eventTime);

                    String json = String.format(
                            "{\"eventId\":\"%s\",\"activity\":\"%s\",\"correlationKeys\":[\"%s\",\"%s\"],\"timestamp\":\"%s\"}",
                            eventId, activity, ck1, ck2, timestamp
                    );

                    System.out.println(json);
                    eventCounter++;
                }
                System.out.println(); // spacing between application groups
            }
            System.out.println("/* ---- End of journey j=" + j + " ---- */\n");
        }
    }
}
