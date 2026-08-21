package com.settle.balances;

import com.settle.activity.ActivityService;
import com.settle.auth.SecurityConfig;
import com.settle.common.Money;
import com.settle.groups.GroupService;
import com.settle.people.PeopleService;
import com.settle.people.Person;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    private final GroupService groups;
    private final BalanceService balances;
    private final ActivityService activity;
    private final PeopleService people;

    public HomeController(
            GroupService groups,
            BalanceService balances,
            ActivityService activity,
            PeopleService people) {
        this.groups = groups;
        this.balances = balances;
        this.activity = activity;
        this.people = people;
    }

    @GetMapping("/api/home")
    public Map<String, Object> home() {
        UUID userId = SecurityConfig.currentUserId();
        Person me = people.requireForUser(userId);
        BigDecimal overall = balances.overallNet(me.getId());
        BigDecimal youOwe = overall.signum() < 0 ? overall.abs() : Money.fromPaise(0);
        BigDecimal youAreOwed = overall.signum() > 0 ? overall : Money.fromPaise(0);
        List<Map<String, Object>> groupRows = new ArrayList<>();
        for (Map<String, Object> g : groups.listMine(userId)) {
            UUID gid = (UUID) g.get("id");
            var nets = balances.netsForGroup(gid);
            BigDecimal net = nets.getOrDefault(me.getId(), Money.fromPaise(0));
            Map<String, Object> row = new LinkedHashMap<>(g);
            row.put("yourNet", net.toPlainString());
            groupRows.add(row);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("you", people.view(me));
        body.put("youOwe", youOwe.toPlainString());
        body.put("youAreOwed", youAreOwed.toPlainString());
        body.put("groups", groupRows);
        body.put("activity", activity.feedFor(me.getId(), 20));
        return body;
    }

    @GetMapping("/api/groups/{id}/balances")
    public Map<String, Object> groupBalances(@PathVariable UUID id) {
        return balances.groupBalances(SecurityConfig.currentUserId(), id);
    }

    @GetMapping("/api/groups/{id}/settlement-plan")
    public Map<String, Object> plan(@PathVariable UUID id) {
        return balances.settlementPlan(SecurityConfig.currentUserId(), id);
    }

    @GetMapping("/api/activity")
    public Map<String, Object> activity() {
        Person me = people.requireForUser(SecurityConfig.currentUserId());
        return Map.of("activity", activity.feedFor(me.getId(), 50));
    }
}
