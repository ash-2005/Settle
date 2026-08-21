package com.settle.expenses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.settle.common.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SplitCalculatorTest {

    @Test
    void exactMustSumToTotal() {
        UUID a = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID b = UUID.fromString("00000000-0000-0000-0000-000000000002");
        var req = new CreateExpenseRequest(
                new BigDecimal("100.00"),
                "x",
                null,
                List.of(),
                List.of(a, b),
                "EXACT",
                Map.of(a.toString(), new BigDecimal("40.00"), b.toString(), new BigDecimal("50.00")),
                null,
                null,
                null);
        assertThrows(Exception.class, () -> SplitCalculator.shares(req));
    }

    @Test
    void sharesProportional() {
        UUID a = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID b = UUID.fromString("00000000-0000-0000-0000-000000000002");
        var req = new CreateExpenseRequest(
                new BigDecimal("90.00"),
                "x",
                null,
                List.of(),
                List.of(a, b),
                "SHARES",
                null,
                null,
                Map.of(a.toString(), 2, b.toString(), 1),
                null);
        var shares = SplitCalculator.shares(req);
        assertEquals(Money.of("60.00"), shares.get(0).amount());
        assertEquals(Money.of("30.00"), shares.get(1).amount());
    }
}
