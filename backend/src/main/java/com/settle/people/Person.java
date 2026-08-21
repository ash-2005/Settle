package com.settle.people;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "people")
public class Person {

    @Id
    private UUID id;

    private String phone;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static Person forUser(UUID userId, String phone, String displayName) {
        Person p = base(displayName, userId);
        p.phone = phone;
        p.userId = userId;
        return p;
    }

    public static Person pending(String phone, String displayName, UUID createdByUserId) {
        Person p = base(displayName, createdByUserId);
        p.phone = phone;
        p.createdByUserId = createdByUserId;
        return p;
    }

    private static Person base(String displayName, UUID createdBy) {
        Person p = new Person();
        Instant now = Instant.now();
        p.id = UUID.randomUUID();
        p.displayName = displayName;
        p.createdByUserId = createdBy;
        p.createdAt = now;
        p.updatedAt = now;
        return p;
    }

    public void attachUser(UUID userId, String displayName) {
        this.userId = userId;
        this.displayName = displayName;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getPhone() {
        return phone;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }
}
