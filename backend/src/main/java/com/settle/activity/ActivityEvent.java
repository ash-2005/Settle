package com.settle.activity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "activity_events")
public class ActivityEvent {

    @Id
    private UUID id;

    @Column(name = "actor_person_id")
    private UUID actorPersonId;

    @Column(nullable = false)
    private String action;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "audience_scope", nullable = false)
    private String audienceScope;

    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload = "{}";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static ActivityEvent of(
            UUID actorPersonId,
            String action,
            String entityType,
            UUID entityId,
            String audienceScope,
            String payloadJson) {
        ActivityEvent e = new ActivityEvent();
        e.id = UUID.randomUUID();
        e.actorPersonId = actorPersonId;
        e.action = action;
        e.entityType = entityType;
        e.entityId = entityId;
        e.audienceScope = audienceScope;
        e.payload = payloadJson;
        e.createdAt = Instant.now();
        return e;
    }

    public UUID getId() {
        return id;
    }

    public UUID getActorPersonId() {
        return actorPersonId;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
