package com.settle.expenses;

import com.settle.activity.ActivityService;
import com.settle.common.ApiException;
import com.settle.common.Money;
import com.settle.groups.GroupService;
import com.settle.people.PeopleService;
import com.settle.people.Person;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseService {

    private final ExpenseRepository expenses;
    private final ExpensePayerRepository payers;
    private final ExpenseParticipantRepository participants;
    private final GroupService groups;
    private final PeopleService people;
    private final ActivityService activity;

    public ExpenseService(
            ExpenseRepository expenses,
            ExpensePayerRepository payers,
            ExpenseParticipantRepository participants,
            GroupService groups,
            PeopleService people,
            ActivityService activity) {
        this.expenses = expenses;
        this.payers = payers;
        this.participants = participants;
        this.groups = groups;
        this.people = people;
        this.activity = activity;
    }

    @Transactional
    public Map<String, Object> create(UUID userId, CreateExpenseRequest req) {
        if (req.description() == null || req.description().isBlank()) {
            throw ApiException.bad("description required");
        }
        Money.requirePositive(req.amount());
        Person me = people.requireForUser(userId);

        if (req.groupId() != null) {
            groups.visibleGroup(userId, req.groupId());
        }

        List<CreateExpenseRequest.PayerIn> payerIns = req.payers();
        if (payerIns == null || payerIns.isEmpty()) {
            payerIns = List.of(new CreateExpenseRequest.PayerIn(me.getId(), req.amount()));
        }
        BigDecimal paid = BigDecimal.ZERO;
        Set<UUID> payerIds = new HashSet<>();
        for (var p : payerIns) {
            Money.requirePositive(p.amount());
            people.require(p.personId());
            if (req.groupId() != null && !groups.isActiveMember(req.groupId(), p.personId())) {
                throw ApiException.bad("payer is not an active group member");
            }
            paid = paid.add(Money.of(p.amount()));
            payerIds.add(p.personId());
        }
        if (paid.compareTo(Money.of(req.amount())) != 0) {
            throw ApiException.bad("payer amounts must sum to the expense");
        }

        List<UUID> participantIds = req.participantIds();
        if (participantIds == null || participantIds.isEmpty()) {
            throw ApiException.bad("need participants");
        }
        for (UUID pid : participantIds) {
            people.require(pid);
            if (req.groupId() != null && !groups.isActiveMember(req.groupId(), pid)) {
                throw ApiException.bad("participant is not an active group member");
            }
        }

        String method = req.splitMethod() == null ? "EQUAL" : req.splitMethod().toUpperCase();
        var shares = SplitCalculator.shares(new CreateExpenseRequest(
                Money.of(req.amount()),
                req.description().trim(),
                req.groupId(),
                payerIns,
                participantIds,
                method,
                req.exactAmounts(),
                req.percentages(),
                req.shareCounts(),
                req.expenseDate()));

        Expense expense = expenses.save(Expense.create(
                req.groupId(),
                userId,
                req.description().trim(),
                Money.of(req.amount()),
                method,
                req.expenseDate()));

        for (var p : payerIns) {
            payers.save(ExpensePayer.of(expense.getId(), p.personId(), Money.of(p.amount())));
        }
        for (var s : shares) {
            participants.save(ExpenseParticipant.of(expense.getId(), s.personId(), s.amount()));
        }

        Set<UUID> audience = new HashSet<>(payerIds);
        shares.forEach(s -> audience.add(s.personId()));
        activity.record(
                me.getId(),
                "EXPENSE_ADDED",
                "EXPENSE",
                expense.getId(),
                "PARTICIPANTS",
                Map.of(
                        "description", expense.getDescription(),
                        "amount", expense.getAmount().toPlainString(),
                        "groupId", expense.getGroupId() == null ? "" : expense.getGroupId().toString()),
                audience);

        return view(userId, expense.getId());
    }

    public Map<String, Object> view(UUID userId, UUID expenseId) {
        Expense e = expenses.findById(expenseId).orElseThrow(ApiException::notFound);
        assertCanSee(userId, e);
        return toView(e);
    }

    public List<Map<String, Object>> listGroup(UUID userId, UUID groupId) {
        groups.visibleGroup(userId, groupId);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Expense e : expenses.findByGroupIdAndStatusOrderByCreatedAtDesc(groupId, "ACTIVE")) {
            out.add(toView(e));
        }
        return out;
    }

    public List<Map<String, Object>> listWithPerson(UUID userId, UUID otherPersonId) {
        Person me = people.requireForUser(userId);
        people.require(otherPersonId);
        Set<UUID> mine = involvedExpenseIds(me.getId());
        Set<UUID> theirs = involvedExpenseIds(otherPersonId);
        mine.retainAll(theirs);
        List<Map<String, Object>> out = new ArrayList<>();
        for (UUID id : mine) {
            Expense e = expenses.findById(id).orElse(null);
            if (e != null && e.getGroupId() == null && "ACTIVE".equals(e.getStatus())) {
                out.add(toView(e));
            }
        }
        out.sort((a, b) -> b.get("createdAt").toString().compareTo(a.get("createdAt").toString()));
        return out;
    }

    @Transactional
    public Map<String, Object> delete(UUID userId, UUID expenseId) {
        Expense e = expenses.findById(expenseId).orElseThrow(ApiException::notFound);
        assertCanSee(userId, e);
        if (!e.getCreatedByUserId().equals(userId)) {
            throw ApiException.bad("only the creator can delete directly in V1");
        }
        e.softDelete();
        expenses.save(e);
        Person me = people.requireForUser(userId);
        activity.record(
                me.getId(),
                "EXPENSE_DELETED",
                "EXPENSE",
                e.getId(),
                "PARTICIPANTS",
                Map.of("description", e.getDescription(), "amount", e.getAmount().toPlainString()),
                audienceOf(e));
        return Map.of("ok", true, "status", "DELETED");
    }

    private void assertCanSee(UUID userId, Expense e) {
        Person me = people.requireForUser(userId);
        if (e.getGroupId() != null) {
            groups.visibleGroup(userId, e.getGroupId());
            return;
        }
        boolean involved = payers.findByExpenseId(e.getId()).stream().anyMatch(p -> p.getPersonId().equals(me.getId()))
                || participants.findByExpenseId(e.getId()).stream().anyMatch(p -> p.getPersonId().equals(me.getId()));
        if (!involved) {
            throw ApiException.notFound();
        }
    }

    private Set<UUID> involvedExpenseIds(UUID personId) {
        Set<UUID> ids = new HashSet<>();
        payers.findByPersonId(personId).forEach(p -> ids.add(p.getExpenseId()));
        participants.findByPersonId(personId).forEach(p -> ids.add(p.getExpenseId()));
        return ids;
    }

    private Set<UUID> audienceOf(Expense e) {
        Set<UUID> a = new HashSet<>();
        payers.findByExpenseId(e.getId()).forEach(p -> a.add(p.getPersonId()));
        participants.findByExpenseId(e.getId()).forEach(p -> a.add(p.getPersonId()));
        return a;
    }

    private Map<String, Object> toView(Expense e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", e.getId());
        body.put("groupId", e.getGroupId());
        body.put("description", e.getDescription());
        body.put("amount", e.getAmount().toPlainString());
        body.put("currency", e.getCurrency());
        body.put("splitMethod", e.getSplitMethod());
        body.put("status", e.getStatus());
        body.put("expenseDate", e.getExpenseDate().toString());
        body.put("createdAt", e.getCreatedAt().toString());
        List<Map<String, Object>> payerViews = new ArrayList<>();
        for (ExpensePayer p : payers.findByExpenseId(e.getId())) {
            payerViews.add(Map.of(
                    "person", people.view(people.require(p.getPersonId())),
                    "amount", p.getAmount().toPlainString()));
        }
        List<Map<String, Object>> shareViews = new ArrayList<>();
        for (ExpenseParticipant p : participants.findByExpenseId(e.getId())) {
            shareViews.add(Map.of(
                    "person", people.view(people.require(p.getPersonId())),
                    "shareAmount", p.getShareAmount().toPlainString()));
        }
        body.put("payers", payerViews);
        body.put("participants", shareViews);
        return body;
    }
}
