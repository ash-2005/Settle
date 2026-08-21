package com.settle.expenses;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CreateExpenseRequest(
        BigDecimal amount,
        String description,
        UUID groupId,
        List<PayerIn> payers,
        List<UUID> participantIds,
        String splitMethod,
        Map<String, BigDecimal> exactAmounts,
        Map<String, BigDecimal> percentages,
        Map<String, Integer> shareCounts,
        LocalDate expenseDate) {

    public record PayerIn(UUID personId, BigDecimal amount) {}
}
