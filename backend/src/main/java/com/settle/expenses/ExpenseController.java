package com.settle.expenses;

import com.settle.auth.SecurityConfig;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExpenseController {

    private final ExpenseService expenses;

    public ExpenseController(ExpenseService expenses) {
        this.expenses = expenses;
    }

    @PostMapping("/api/expenses")
    public Map<String, Object> create(@RequestBody CreateExpenseRequest body) {
        return expenses.create(SecurityConfig.currentUserId(), body);
    }

    @GetMapping("/api/expenses/{id}")
    public Map<String, Object> one(@PathVariable UUID id) {
        return expenses.view(SecurityConfig.currentUserId(), id);
    }

    @PostMapping("/api/expenses/{id}/delete")
    public Map<String, Object> delete(@PathVariable UUID id) {
        return expenses.delete(SecurityConfig.currentUserId(), id);
    }

    @GetMapping("/api/groups/{id}/expenses")
    public List<Map<String, Object>> groupExpenses(@PathVariable UUID id) {
        return expenses.listGroup(SecurityConfig.currentUserId(), id);
    }

    @GetMapping("/api/people/{id}/expenses")
    public List<Map<String, Object>> withPerson(@PathVariable UUID id) {
        return expenses.listWithPerson(SecurityConfig.currentUserId(), id);
    }
}
