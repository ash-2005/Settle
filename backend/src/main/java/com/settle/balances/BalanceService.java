package com.settle.balances;

import com.settle.common.Money;
import com.settle.expenses.Expense;
import com.settle.expenses.ExpenseParticipantRepository;
import com.settle.expenses.ExpensePayerRepository;
import com.settle.expenses.ExpenseRepository;
import com.settle.groups.GroupMember;
import com.settle.groups.GroupService;
import com.settle.people.PeopleService;
import com.settle.people.Person;
import com.settle.settlements.SettlementPlanner;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BalanceService {

    private final ExpenseRepository expenses;
    private final ExpensePayerRepository payers;
    private final ExpenseParticipantRepository participants;
    private final GroupService groups;
    private final PeopleService people;

    public BalanceService(
            ExpenseRepository expenses,
            ExpensePayerRepository payers,
            ExpenseParticipantRepository participants,
            GroupService groups,
            PeopleService people) {
        this.expenses = expenses;
        this.payers = payers;
        this.participants = participants;
        this.groups = groups;
        this.people = people;
    }

    public Map<String, Object> groupBalances(UUID userId, UUID groupId) {
        groups.visibleGroup(userId, groupId);
        Map<UUID, BigDecimal> nets = netsForGroup(groupId);
        Person me = people.requireForUser(userId);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (GroupMember m : groups.activeMembers(groupId)) {
            BigDecimal net = nets.getOrDefault(m.getPersonId(), Money.fromPaise(0));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("person", people.view(people.require(m.getPersonId())));
            row.put("net", net.toPlainString());
            rows.add(row);
        }
        List<Map<String, Object>> why = whyIOwe(me.getId(), groupId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("you", people.view(me));
        body.put("yourNet", nets.getOrDefault(me.getId(), Money.fromPaise(0)).toPlainString());
        body.put("members", rows);
        body.put("why", why);
        return body;
    }

    public Map<String, Object> settlementPlan(UUID userId, UUID groupId) {
        groups.visibleGroup(userId, groupId);
        Map<UUID, BigDecimal> nets = netsForGroup(groupId);
        List<Map<String, Object>> transfers = new ArrayList<>();
        for (var t : SettlementPlanner.plan(nets)) {
            transfers.add(Map.of(
                    "from", people.view(people.require(t.fromPersonId())),
                    "to", people.view(people.require(t.toPersonId())),
                    "amount", t.amount().toPlainString()));
        }
        return Map.of(
                "note", "Optimized plan from net balances. This does not claim the minimum possible number of payments.",
                "transfers", transfers);
    }

    public Map<UUID, BigDecimal> netsForGroup(UUID groupId) {
        Map<UUID, BigDecimal> nets = new HashMap<>();
        for (Expense e : expenses.findByGroupIdAndStatusOrderByCreatedAtDesc(groupId, "ACTIVE")) {
            applyExpense(e.getId(), nets);
        }
        return nets;
    }

    public void applyExpense(UUID expenseId, Map<UUID, BigDecimal> nets) {
        for (var p : payers.findByExpenseId(expenseId)) {
            nets.merge(p.getPersonId(), p.getAmount(), BigDecimal::add);
        }
        for (var p : participants.findByExpenseId(expenseId)) {
            nets.merge(p.getPersonId(), p.getShareAmount().negate(), BigDecimal::add);
        }
    }

    /** Paid minus share across every ACTIVE expense this person is on (groups + individual). */
    public BigDecimal overallNet(UUID personId) {
        Map<UUID, BigDecimal> nets = new HashMap<>();
        java.util.Set<UUID> ids = new java.util.HashSet<>();
        payers.findByPersonId(personId).forEach(p -> ids.add(p.getExpenseId()));
        participants.findByPersonId(personId).forEach(p -> ids.add(p.getExpenseId()));
        for (UUID id : ids) {
            expenses.findById(id).ifPresent(e -> {
                if ("ACTIVE".equals(e.getStatus())) {
                    applyExpense(id, nets);
                }
            });
        }
        return nets.getOrDefault(personId, Money.fromPaise(0));
    }

    public List<Map<String, Object>> whyIOwe(UUID personId, UUID groupId) {
        List<Map<String, Object>> lines = new ArrayList<>();
        for (Expense e : expenses.findByGroupIdAndStatusOrderByCreatedAtDesc(groupId, "ACTIVE")) {
            BigDecimal paid = payers.findByExpenseId(e.getId()).stream()
                    .filter(p -> p.getPersonId().equals(personId))
                    .map(p -> p.getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal share = participants.findByExpenseId(e.getId()).stream()
                    .filter(p -> p.getPersonId().equals(personId))
                    .map(p -> p.getShareAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal delta = paid.subtract(share);
            if (delta.signum() != 0) {
                lines.add(Map.of(
                        "expenseId", e.getId(),
                        "description", e.getDescription(),
                        "delta", delta.toPlainString(),
                        "amount", e.getAmount().toPlainString()));
            }
        }
        return lines;
    }
}
