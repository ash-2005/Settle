package com.settle.expenses;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findByGroupIdAndStatusOrderByCreatedAtDesc(UUID groupId, String status);
}
