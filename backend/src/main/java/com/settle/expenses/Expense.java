package com.settle.expenses;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    private UUID id;

    @Column(name = "group_id")
    private UUID groupId;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency = "INR";

    @Column(name = "split_method", nullable = false)
    private String splitMethod;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(name = "approval_status", nullable = false)
    private String approvalStatus = "NOT_REQUIRED";

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static Expense create(
            UUID groupId,
            UUID createdByUserId,
            String description,
            BigDecimal amount,
            String splitMethod,
            LocalDate date) {
        Expense e = new Expense();
        Instant now = Instant.now();
        e.id = UUID.randomUUID();
        e.groupId = groupId;
        e.createdByUserId = createdByUserId;
        e.description = description;
        e.amount = amount;
        e.splitMethod = splitMethod;
        e.expenseDate = date == null ? LocalDate.now() : date;
        e.createdAt = now;
        e.updatedAt = now;
        return e;
    }

    public void softDelete() {
        this.status = "DELETED";
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getSplitMethod() {
        return splitMethod;
    }

    public String getStatus() {
        return status;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
