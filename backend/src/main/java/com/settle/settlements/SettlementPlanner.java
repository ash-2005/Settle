package com.settle.settlements;

import com.settle.common.Money;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;

/**
 * Greedy two-heap netting. Clears nets with simple pairing.
 * Does not claim a minimum number of payments (that problem is NP-hard).
 */
public final class SettlementPlanner {

    private SettlementPlanner() {}

    public record Transfer(UUID fromPersonId, UUID toPersonId, BigDecimal amount) {}

    private record Node(UUID personId, long paise) {}

    public static List<Transfer> plan(Map<UUID, BigDecimal> nets) {
        Comparator<Node> maxPaise = Comparator.comparingLong(Node::paise).reversed();
        PriorityQueue<Node> creditors = new PriorityQueue<>(maxPaise);
        PriorityQueue<Node> debtors = new PriorityQueue<>(maxPaise);

        for (var e : nets.entrySet()) {
            long paise = Money.toPaise(e.getValue());
            if (paise > 0) {
                creditors.add(new Node(e.getKey(), paise));
            } else if (paise < 0) {
                debtors.add(new Node(e.getKey(), -paise));
            }
        }

        List<Transfer> transfers = new ArrayList<>();
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Node c = creditors.poll();
            Node d = debtors.poll();
            long pay = Math.min(c.paise(), d.paise());
            transfers.add(new Transfer(d.personId(), c.personId(), Money.fromPaise(pay)));
            if (c.paise() > pay) {
                creditors.add(new Node(c.personId(), c.paise() - pay));
            }
            if (d.paise() > pay) {
                debtors.add(new Node(d.personId(), d.paise() - pay));
            }
        }
        return transfers;
    }
}
