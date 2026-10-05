package com.settle.payments;

import com.settle.activity.ActivityService;
import com.settle.common.ApiException;
import com.settle.common.Money;
import com.settle.groups.GroupService;
import com.settle.people.PeopleService;
import com.settle.people.Person;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Payments settle balances; they never rewrite expenses. Only CONFIRMED rows count. */
@Service
public class PaymentService {

    private final JdbcTemplate jdbc;
    private final GroupService groups;
    private final PeopleService people;
    private final ActivityService activity;

    public PaymentService(JdbcTemplate jdbc, GroupService groups, PeopleService people, ActivityService activity) {
        this.jdbc = jdbc;
        this.groups = groups;
        this.people = people;
        this.activity = activity;
    }

    @Transactional
    public Map<String, Object> markPaid(UUID userId, UUID groupId, UUID toPersonId, BigDecimal rawAmount) {
        groups.visibleGroup(userId, groupId);
        Person me = people.requireForUser(userId);
        if (toPersonId == null || rawAmount == null) {
            throw ApiException.bad("toPersonId and amount are required");
        }
        BigDecimal amount = Money.of(rawAmount);
        if (amount.signum() <= 0) {
            throw ApiException.bad("amount must be positive");
        }
        if (toPersonId.equals(me.getId())) {
            throw ApiException.bad("you cannot pay yourself");
        }
        Integer inGroup = jdbc.queryForObject(
                "SELECT count(*) FROM group_members WHERE group_id = ? AND person_id = ?",
                Integer.class, groupId, toPersonId);
        if (inGroup == null || inGroup == 0) {
            throw ApiException.bad("recipient is not in this group");
        }
        UUID id = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO settlement_payments (id, group_id, from_person_id, to_person_id, amount) VALUES (?,?,?,?,?)",
                id, groupId, me.getId(), toPersonId, amount);
        activity.record(
                me.getId(), "PAYMENT_MARKED", "PAYMENT", id, "PARTICIPANTS",
                Map.of("amount", amount.toPlainString(), "groupId", groupId.toString()),
                Set.of(toPersonId));
        return view(id);
    }

    @Transactional
    public Map<String, Object> resolve(UUID userId, UUID paymentId, boolean confirm) {
        Person me = people.requireForUser(userId);
        Map<String, Object> row = jdbc.queryForList(
                "SELECT * FROM settlement_payments WHERE id = ? FOR UPDATE", paymentId)
                .stream().findFirst().orElseThrow(ApiException::notFound);
        // Anyone who is not the recipient gets a 404, not a hint that the payment exists.
        if (!row.get("to_person_id").equals(me.getId())) {
            throw ApiException.notFound();
        }
        if (!"PENDING".equals(row.get("status"))) {
            throw ApiException.bad("payment is already " + row.get("status"));
        }
        jdbc.update(
                "UPDATE settlement_payments SET status = ?, resolved_at = now() WHERE id = ?",
                confirm ? "CONFIRMED" : "REJECTED", paymentId);
        UUID from = (UUID) row.get("from_person_id");
        activity.record(
                me.getId(), confirm ? "PAYMENT_CONFIRMED" : "PAYMENT_REJECTED", "PAYMENT", paymentId,
                "PARTICIPANTS",
                Map.of("amount", ((BigDecimal) row.get("amount")).toPlainString(),
                        "groupId", row.get("group_id").toString()),
                Set.of(from));
        return view(paymentId);
    }

    public List<Map<String, Object>> listGroup(UUID userId, UUID groupId) {
        groups.visibleGroup(userId, groupId);
        List<Map<String, Object>> out = new ArrayList<>();
        for (UUID id : jdbc.queryForList(
                "SELECT id FROM settlement_payments WHERE group_id = ? ORDER BY created_at DESC LIMIT 100",
                UUID.class, groupId)) {
            out.add(view(id));
        }
        return out;
    }

    /** Confirmed payments as net adjustments: the payer's debt shrinks, the recipient's credit shrinks. */
    public void applyConfirmed(UUID groupId, Map<UUID, BigDecimal> nets) {
        jdbc.query(
                "SELECT from_person_id, to_person_id, amount FROM settlement_payments WHERE group_id = ? AND status = 'CONFIRMED'",
                rs -> {
                    BigDecimal a = rs.getBigDecimal("amount");
                    nets.merge((UUID) rs.getObject("from_person_id"), a, BigDecimal::add);
                    nets.merge((UUID) rs.getObject("to_person_id"), a.negate(), BigDecimal::add);
                },
                groupId);
    }

    /** Same adjustment across every group, for one person's overall net on the home screen. */
    public BigDecimal confirmedAdjustment(UUID personId) {
        BigDecimal paid = jdbc.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM settlement_payments WHERE from_person_id = ? AND status = 'CONFIRMED'",
                BigDecimal.class, personId);
        BigDecimal received = jdbc.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM settlement_payments WHERE to_person_id = ? AND status = 'CONFIRMED'",
                BigDecimal.class, personId);
        return paid.subtract(received);
    }

    private Map<String, Object> view(UUID id) {
        Map<String, Object> r = jdbc.queryForMap("SELECT * FROM settlement_payments WHERE id = ?", id);
        Map<String, Object> v = new LinkedHashMap<>();
        v.put("id", r.get("id"));
        v.put("groupId", r.get("group_id"));
        v.put("from", people.view(people.require((UUID) r.get("from_person_id"))));
        v.put("to", people.view(people.require((UUID) r.get("to_person_id"))));
        v.put("amount", ((BigDecimal) r.get("amount")).toPlainString());
        v.put("status", r.get("status"));
        v.put("createdAt", r.get("created_at").toString());
        return v;
    }
}
