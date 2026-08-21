package com.settle.people;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invites")
public class Invite {

    @Id
    private UUID id;

    @Column(name = "person_id", nullable = false)
    private UUID personId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    public static Invite create(UUID personId, UUID createdByUserId) {
        Invite i = new Invite();
        i.id = UUID.randomUUID();
        i.personId = personId;
        i.token = UUID.randomUUID().toString().replace("-", "");
        i.createdByUserId = createdByUserId;
        i.expiresAt = Instant.now().plusSeconds(60L * 60 * 24 * 14);
        return i;
    }

    public void accept() {
        this.acceptedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getPersonId() {
        return personId;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }
}
