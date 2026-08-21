package com.settle.expenses;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseParticipantRepository extends JpaRepository<ExpenseParticipant, ExpenseParticipant.PK> {
    List<ExpenseParticipant> findByExpenseId(UUID expenseId);

    List<ExpenseParticipant> findByPersonId(UUID personId);
}
