package com.settle.ai;

import com.settle.common.Money;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deterministic reading of common “I paid X for Y, split with …” sentences.
 * Used when no LLM key is set, and as a fallback if Gemini fails.
 * Does not write expenses.
 */
public final class ExpenseDraftParser {

    private static final Pattern AMOUNT = Pattern.compile("(?:rs\\.?|inr|₹)?\\s*([0-9]+(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern FOR = Pattern.compile("(?:for|on)\\s+([a-zA-Z][a-zA-Z0-9 \\-]{1,40})", Pattern.CASE_INSENSITIVE);

    private ExpenseDraftParser() {}

    public static Map<String, Object> parse(String text, UUID meId, List<NamedPerson> people) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("say or type what you paid");
        }
        String lower = text.toLowerCase(Locale.ROOT);
        Matcher amt = AMOUNT.matcher(text.replace(",", ""));
        if (!amt.find()) {
            throw new IllegalArgumentException("could not find an amount in that sentence");
        }
        BigDecimal amount = Money.of(amt.group(1));

        String description = "Expense";
        Matcher forWhat = FOR.matcher(text);
        if (forWhat.find()) {
            description = forWhat.group(1).trim();
            description = description.replaceAll("(?i)\\s+split.*", "").trim();
        }

        List<UUID> participants = new ArrayList<>();
        participants.add(meId);
        for (NamedPerson p : people) {
            if (p.id().equals(meId)) {
                continue;
            }
            String first = firstName(p.displayName());
            if (first.length() >= 3 && lower.contains(first.toLowerCase(Locale.ROOT))) {
                participants.add(p.id());
            }
        }

        UUID payer = meId;
        for (NamedPerson p : people) {
            String first = firstName(p.displayName());
            if (first.length() >= 3 && lower.matches(".*" + Pattern.quote(first.toLowerCase(Locale.ROOT)) + "\\s+paid.*")) {
                payer = p.id();
            }
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("amount", amount.toPlainString());
        out.put("description", capitalize(description));
        out.put("payerId", payer.toString());
        out.put("participantIds", participants.stream().map(UUID::toString).toList());
        out.put("splitMethod", "EQUAL");
        out.put("source", "parser");
        out.put("note", "Parsed on the server. Review every field before saving — this is not posted yet.");
        return out;
    }

    private static String firstName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return "";
        }
        return displayName.trim().split("\\s+")[0];
    }

    private static String capitalize(String s) {
        if (s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }

    public record NamedPerson(UUID id, String displayName) {}
}
