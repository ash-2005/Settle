package com.settle.activity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "activity_audience")
@IdClass(ActivityAudience.PK.class)
public class ActivityAudience {

    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @Id
    @Column(name = "person_id")
    private UUID personId;

    public static ActivityAudience of(UUID eventId, UUID personId) {
        ActivityAudience a = new ActivityAudience();
        a.eventId = eventId;
        a.personId = personId;
        return a;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getPersonId() {
        return personId;
    }

    public static class PK implements Serializable {
        private UUID eventId;
        private UUID personId;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof PK pk)) {
                return false;
            }
            return Objects.equals(eventId, pk.eventId) && Objects.equals(personId, pk.personId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventId, personId);
        }
    }
}
