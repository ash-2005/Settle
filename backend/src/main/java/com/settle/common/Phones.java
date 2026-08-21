package com.settle.common;

import java.util.regex.Pattern;

public final class Phones {

    private static final Pattern DIGITS = Pattern.compile("[^0-9+]");

    private Phones() {}

    /** 10-digit Indian numbers become +91…; otherwise keep a leading +. */
    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            throw ApiException.bad("phone required");
        }
        String trimmed = DIGITS.matcher(raw.trim()).replaceAll("");
        if (trimmed.startsWith("+")) {
            if (trimmed.length() < 8) {
                throw ApiException.bad("phone looks too short");
            }
            return trimmed;
        }
        if (trimmed.length() == 10) {
            return "+91" + trimmed;
        }
        if (trimmed.length() == 12 && trimmed.startsWith("91")) {
            return "+" + trimmed;
        }
        throw ApiException.bad("use a 10-digit Indian number or include country code");
    }
}
