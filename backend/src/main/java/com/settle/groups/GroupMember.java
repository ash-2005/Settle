package com.settle.groups;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "group_members")
public class GroupMember {

    @Id
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "person_id", nullable = false)
    private UUID personId;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String status;

    @Column(name = "left_at")
    private Instant leftAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static GroupMember join(UUID groupId, UUID personId, String role) {
        GroupMember m = new GroupMember();
        m.id = UUID.randomUUID();
        m.groupId = groupId;
        m.personId = personId;
        m.role = role;
        m.status = "ACTIVE";
        m.createdAt = Instant.now();
        return m;
    }

    public void markLeft(String status) {
        this.status = status;
        this.leftAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public UUID getPersonId() {
        return personId;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }
}
