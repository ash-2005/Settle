package com.settle.expenses;

import com.settle.common.ApiException;
import com.settle.common.Money;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

final class SplitCalculator {

    private SplitCalculator() {}

    static List<Share> shares(CreateExpenseRequest req) {
        List<UUID> ids = new ArrayList<>(req.participantIds());
        if (ids.isEmpty()) {
            throw ApiException.bad("need participants");
        }
        ids.sort(Comparator.naturalOrder());
        String method = req.splitMethod() == null ? "EQUAL" : req.splitMethod().toUpperCase();
        List<BigDecimal> amounts = switch (method) {
            case "EQUAL" -> Money.splitEqual(req.amount(), ids);
            case "EXACT" -> exact(req, ids);
            case "PERCENTAGE" -> percent(req, ids);
            case "SHARES" -> byShares(req, ids);
            default -> throw ApiException.bad("unknown split method");
        };
        List<Share> out = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            out.add(new Share(ids.get(i), amounts.get(i)));
        }
        if (!Money.sumsTo(amounts, req.amount())) {
            throw ApiException.bad("shares must sum to the expense amount");
        }
        return out;
    }

    private static List<BigDecimal> exact(CreateExpenseRequest req, List<UUID> ids) {
        if (req.exactAmounts() == null || req.exactAmounts().size() != ids.size()) {
            throw ApiException.bad("exact split needs an amount per participant");
        }
        List<BigDecimal> ordered = new ArrayList<>();
        for (UUID id : ids) {
            BigDecimal amt = req.exactAmounts().get(id.toString());
            if (amt == null) {
                amt = req.exactAmounts().get(id.toString().toLowerCase());
            }
            if (amt == null) {
                throw ApiException.bad("missing exact amount for a participant");
            }
            ordered.add(Money.of(amt));
        }
        if (!Money.sumsTo(ordered, req.amount())) {
            throw ApiException.bad("exact amounts must sum to the expense");
        }
        return ordered;
    }

    private static List<BigDecimal> percent(CreateExpenseRequest req, List<UUID> ids) {
        if (req.percentages() == null || req.percentages().size() != ids.size()) {
            throw ApiException.bad("percentage split needs a percent per participant");
        }
        List<BigDecimal> percents = new ArrayList<>();
        for (UUID id : ids) {
            BigDecimal p = req.percentages().get(id.toString());
            if (p == null) {
                throw ApiException.bad("missing percentage for a participant");
            }
            percents.add(p);
        }
        return Money.splitByPercentage(req.amount(), percents);
    }

    private static List<BigDecimal> byShares(CreateExpenseRequest req, List<UUID> ids) {
        if (req.shareCounts() == null || req.shareCounts().size() != ids.size()) {
            throw ApiException.bad("shares split needs a share count per participant");
        }
        List<Integer> counts = new ArrayList<>();
        for (UUID id : ids) {
            Integer n = req.shareCounts().get(id.toString());
            if (n == null || n <= 0) {
                throw ApiException.bad("each participant needs a positive share count");
            }
            counts.add(n);
        }
        return Money.splitByShares(req.amount(), counts);
    }

    record Share(UUID personId, BigDecimal amount) {}
}
