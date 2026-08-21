package com.settle.groups;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "groups")
public class ExpenseGroup {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    @Column(name = "approval_mode", nullable = false)
    private String approvalMode = "OFF";

    @Column(name = "allow_cross_settlement", nullable = false)
    private boolean allowCrossSettlement;

    @Column(name = "deletion_policy", nullable = false)
    private String deletionPolicy = "CREATOR_DIRECT";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static ExpenseGroup create(String name, String type, UUID ownerUserId) {
        ExpenseGroup g = new ExpenseGroup();
        Instant now = Instant.now();
        g.id = UUID.randomUUID();
        g.name = name;
        g.type = type == null || type.isBlank() ? "CUSTOM" : type.toUpperCase();
        g.ownerUserId = ownerUserId;
        g.createdAt = now;
        g.updatedAt = now;
        return g;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public UUID getOwnerUserId() {
        return ownerUserId;
    }

    public boolean isAllowCrossSettlement() {
        return allowCrossSettlement;
    }

    public String getDeletionPolicy() {
        return deletionPolicy;
    }
}
