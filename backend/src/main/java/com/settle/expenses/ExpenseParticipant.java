package com.settle.expenses;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "expense_participants")
@IdClass(ExpenseParticipant.PK.class)
public class ExpenseParticipant {

    @Id
    @Column(name = "expense_id")
    private UUID expenseId;

    @Id
    @Column(name = "person_id")
    private UUID personId;

    @Column(name = "share_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal shareAmount;

    @Column(name = "share_count")
    private Integer shareCount;

    private BigDecimal percentage;

    public static ExpenseParticipant of(UUID expenseId, UUID personId, BigDecimal shareAmount) {
        ExpenseParticipant p = new ExpenseParticipant();
        p.expenseId = expenseId;
        p.personId = personId;
        p.shareAmount = shareAmount;
        return p;
    }

    public UUID getExpenseId() {
        return expenseId;
    }

    public UUID getPersonId() {
        return personId;
    }

    public BigDecimal getShareAmount() {
        return shareAmount;
    }

    public static class PK implements Serializable {
        private UUID expenseId;
        private UUID personId;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof PK pk)) {
                return false;
            }
            return Objects.equals(expenseId, pk.expenseId) && Objects.equals(personId, pk.personId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(expenseId, personId);
        }
    }
}
