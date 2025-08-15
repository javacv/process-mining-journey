package com.abc.process.mining.journey.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

public final class Hashing {

    private Hashing() {
    }

    public static String journeyId(List<String> correlationKeys, String eventId) {
        // Sort keys for determinism
        String joined = correlationKeys.stream()
                .sorted()
                .collect(Collectors.joining("|")) + "|" + eventId;
        return sha256Hex(joined);
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
