package com.settle.expenses;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpensePayerRepository extends JpaRepository<ExpensePayer, ExpensePayer.PK> {
    List<ExpensePayer> findByExpenseId(UUID expenseId);

    List<ExpensePayer> findByPersonId(UUID personId);
}
