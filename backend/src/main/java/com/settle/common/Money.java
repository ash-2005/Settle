package com.settle.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * All rupees go through here. Never use double for money.
 * Splits work in paise so leftover 1-paise units can be handed out deterministically.
 */
public final class Money {

    public static final int SCALE = 2;
    public static final RoundingMode STORE = RoundingMode.UNNECESSARY;

    private Money() {}

    public static BigDecimal of(BigDecimal raw) {
        if (raw == null) {
            throw new IllegalArgumentException("amount required");
        }
        return raw.setScale(SCALE, RoundingMode.UNNECESSARY);
    }

    public static BigDecimal of(String raw) {
        return of(new BigDecimal(raw));
    }

    public static long toPaise(BigDecimal amount) {
        return of(amount).movePointRight(SCALE).longValueExact();
    }

    public static BigDecimal fromPaise(long paise) {
        return BigDecimal.valueOf(paise).movePointLeft(SCALE).setScale(SCALE, RoundingMode.UNNECESSARY);
    }

    public static void requirePositive(BigDecimal amount) {
        if (of(amount).signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }

    /**
     * Equal split. Remainder paise go to the first people in stable person id order
     * so the shares always sum to the expense.
     */
    public static List<BigDecimal> splitEqual(BigDecimal total, List<UUID> participantIdsSorted) {
        if (participantIdsSorted.isEmpty()) {
            throw new IllegalArgumentException("need at least one participant");
        }
        long paise = toPaise(total);
        int n = participantIdsSorted.size();
        long base = paise / n;
        long rem = paise % n;
        List<BigDecimal> shares = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            shares.add(fromPaise(base + (i < rem ? 1 : 0)));
        }
        return shares;
    }

    public static List<BigDecimal> splitByShares(BigDecimal total, List<Integer> shareCounts) {
        int totalShares = shareCounts.stream().mapToInt(Integer::intValue).sum();
        if (totalShares <= 0) {
            throw new IllegalArgumentException("shares must sum to more than zero");
        }
        long paise = toPaise(total);
        List<BigDecimal> out = new ArrayList<>(shareCounts.size());
        long allocated = 0;
        for (int i = 0; i < shareCounts.size(); i++) {
            if (i == shareCounts.size() - 1) {
                out.add(fromPaise(paise - allocated));
            } else {
                long part = paise * shareCounts.get(i) / totalShares;
                allocated += part;
                out.add(fromPaise(part));
            }
        }
        return out;
    }

    public static List<BigDecimal> splitByPercentage(BigDecimal total, List<BigDecimal> percents) {
        BigDecimal sum = percents.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.compareTo(new BigDecimal("100")) != 0) {
            throw new IllegalArgumentException("percentages must sum to 100");
        }
        long paise = toPaise(total);
        List<BigDecimal> out = new ArrayList<>(percents.size());
        long allocated = 0;
        for (int i = 0; i < percents.size(); i++) {
            if (i == percents.size() - 1) {
                out.add(fromPaise(paise - allocated));
            } else {
                long part = percents.get(i).multiply(BigDecimal.valueOf(paise))
                        .divide(new BigDecimal("100"), 0, RoundingMode.DOWN)
                        .longValueExact();
                allocated += part;
                out.add(fromPaise(part));
            }
        }
        return out;
    }

    public static boolean sumsTo(List<BigDecimal> parts, BigDecimal total) {
        BigDecimal sum = parts.stream().map(Money::of).reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.compareTo(of(total)) == 0;
    }
}
