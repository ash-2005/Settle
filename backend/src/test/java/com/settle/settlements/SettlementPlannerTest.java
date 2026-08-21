package com.settle.settlements;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SettlementPlannerTest {

    @Test
    void twoDebtorsTwoCreditorsClearNets() {
        UUID rahul = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID priya = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID ashmit = UUID.fromString("00000000-0000-0000-0000-000000000003");
        UUID karan = UUID.fromString("00000000-0000-0000-0000-000000000004");
        Map<UUID, BigDecimal> nets = new LinkedHashMap<>();
        nets.put(rahul, new BigDecimal("1500.00"));
        nets.put(priya, new BigDecimal("500.00"));
        nets.put(ashmit, new BigDecimal("-1200.00"));
        nets.put(karan, new BigDecimal("-800.00"));
        var plan = SettlementPlanner.plan(nets);
        BigDecimal moved = plan.stream().map(SettlementPlanner.Transfer::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("2000.00"), moved);
        assertEquals(3, plan.size());
    }
}
