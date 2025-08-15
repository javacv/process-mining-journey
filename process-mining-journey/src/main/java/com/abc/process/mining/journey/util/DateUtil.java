package com.abc.process.mining.journey.util;


import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class DateUtil {

    private static final DateTimeFormatter INDEX_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd").withZone(ZoneOffset.UTC);

    private DateUtil() {
    }

    public static String indexNameForDay(String base, Instant timestamp) {
        return base + "-" + INDEX_FORMAT.format(timestamp);
    }
}

