package com.settle.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void equalSplitRemainderGoesToFirstSortedIds() {
        UUID a = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID b = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID c = UUID.fromString("00000000-0000-0000-0000-000000000003");
        var shares = Money.splitEqual(new BigDecimal("100.00"), List.of(a, b, c));
        assertEquals(new BigDecimal("33.34"), shares.get(0));
        assertEquals(new BigDecimal("33.33"), shares.get(1));
        assertEquals(new BigDecimal("33.33"), shares.get(2));
        assertEquals(new BigDecimal("100.00"), shares.get(0).add(shares.get(1)).add(shares.get(2)));
    }
}
