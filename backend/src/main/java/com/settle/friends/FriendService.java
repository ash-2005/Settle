package com.settle.friends;

import com.settle.expenses.Expense;
import com.settle.expenses.ExpenseParticipant;
import com.settle.expenses.ExpenseParticipantRepository;
import com.settle.expenses.ExpensePayer;
import com.settle.expenses.ExpensePayerRepository;
import com.settle.expenses.ExpenseRepository;
import com.settle.payments.PaymentService;
import com.settle.people.PeopleService;
import com.settle.people.Person;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Pairwise balances ("with each person"), across every group and one-off expense.
 *
 * On one expense, X owes the payer Y: share_X * paid_Y / amount. This is a different view from the
 * group net balance; both are derived from expenses and confirmed payments, never stored.
 */
@Service
public class FriendService {

    private final ExpenseRepository expenses;
    private final ExpensePayerRepository payers;
    private final ExpenseParticipantRepository participants;
    private final PeopleService people;
    private final PaymentService payments;

    public FriendService(
            ExpenseRepository expenses,
            ExpensePayerRepository payers,
            ExpenseParticipantRepository participants,
            PeopleService people,
            PaymentService payments) {
        this.expenses = expenses;
        this.payers = payers;
        this.participants = participants;
        this.people = people;
        this.payments = payments;
    }

    /** Positive = they owe me. Keys are the other people on any ACTIVE expense I am on. */
    public Map<UUID, BigDecimal> pairwise(UUID me) {
        Set<UUID> expenseIds = new HashSet<>();
        payers.findByPersonId(me).forEach(p -> expenseIds.add(p.getExpenseId()));
        participants.findByPersonId(me).forEach(p -> expenseIds.add(p.getExpenseId()));
        Map<UUID, BigDecimal> net = new HashMap<>();
        for (UUID id : expenseIds) {
            Expense e = expenses.findById(id).orElse(null);
            if (e == null || !"ACTIVE".equals(e.getStatus())) {
                continue;
            }
            List<ExpensePayer> ps = payers.findByExpenseId(id);
            List<ExpenseParticipant> ss = participants.findByExpenseId(id);
            BigDecimal total = e.getAmount();
            BigDecimal myPaid = ps.stream().filter(p -> p.getPersonId().equals(me))
                    .map(ExpensePayer::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal myShare = ss.stream().filter(s -> s.getPersonId().equals(me))
                    .map(ExpenseParticipant::getShareAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            for (ExpenseParticipant s : ss) {
                if (!s.getPersonId().equals(me) && myPaid.signum() > 0) {
                    net.merge(s.getPersonId(), s.getShareAmount().multiply(myPaid).divide(total, 6, RoundingMode.HALF_UP), BigDecimal::add);
                }
            }
            for (ExpensePayer p : ps) {
                if (!p.getPersonId().equals(me) && myShare.signum() > 0) {
                    net.merge(p.getPersonId(), myShare.multiply(p.getAmount()).divide(total, 6, RoundingMode.HALF_UP).negate(), BigDecimal::add);
                }
            }
        }
        for (Map.Entry<UUID, BigDecimal> en : net.entrySet()) {
            // If I paid them, they owe me less than before; if they paid me, I owe them less.
            en.setValue(en.getValue().add(payments.confirmedBetween(me, en.getKey())));
        }
        return net;
    }

    public List<Map<String, Object>> friends(UUID userId) {
        Person me = people.requireForUser(userId);
        List<Map<String, Object>> out = new ArrayList<>();
        for (var en : pairwise(me.getId()).entrySet()) {
            BigDecimal rounded = en.getValue().setScale(2, RoundingMode.HALF_UP);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("person", people.view(people.require(en.getKey())));
            row.put("net", rounded.toPlainString());
            out.add(row);
        }
        // Biggest balances first, then by name.
        out.sort(Comparator
                .comparing((Map<String, Object> r) -> new BigDecimal((String) r.get("net")).abs()).reversed()
                .thenComparing(r -> (String) ((Map<?, ?>) r.get("person")).get("displayName"), String.CASE_INSENSITIVE_ORDER));
        return out;
    }

    public Map<String, Object> friend(UUID userId, UUID otherPersonId) {
        Person me = people.requireForUser(userId);
        Person other = people.require(otherPersonId);
        BigDecimal net = pairwise(me.getId()).getOrDefault(otherPersonId, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("person", people.view(other));
        body.put("net", net.toPlainString());
        body.put("pendingPayments", payments.betweenPending(userId, otherPersonId));
        return body;
    }
}
