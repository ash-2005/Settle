package com.settle.activity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.settle.people.PeopleService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityService {

    private final ActivityEventRepository events;
    private final ActivityAudienceRepository audience;
    private final PeopleService people;
    private final ObjectMapper json;

    public ActivityService(
            ActivityEventRepository events,
            ActivityAudienceRepository audience,
            PeopleService people,
            ObjectMapper json) {
        this.events = events;
        this.audience = audience;
        this.people = people;
        this.json = json;
    }

    @Transactional
    public void record(
            UUID actorPersonId,
            String action,
            String entityType,
            UUID entityId,
            String scope,
            Map<String, ?> payload,
            Set<UUID> personIds) {
        String blob;
        try {
            blob = json.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            blob = "{}";
        }
        ActivityEvent event = events.save(ActivityEvent.of(actorPersonId, action, entityType, entityId, scope, blob));
        for (UUID pid : personIds) {
            audience.save(ActivityAudience.of(event.getId(), pid));
        }
    }

    public List<Map<String, Object>> feedFor(UUID personId, int limit) {
        List<ActivityAudience> rows = audience.findByPersonId(personId);
        List<Map<String, Object>> out = new ArrayList<>();
        for (ActivityAudience row : rows) {
            events.findById(row.getEventId()).ifPresent(ev -> out.add(view(ev)));
        }
        out.sort((a, b) -> b.get("createdAt").toString().compareTo(a.get("createdAt").toString()));
        if (out.size() > limit) {
            return out.subList(0, limit);
        }
        return out;
    }

    private Map<String, Object> view(ActivityEvent ev) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", ev.getId());
        m.put("action", ev.getAction());
        m.put("entityType", ev.getEntityType());
        m.put("entityId", ev.getEntityId());
        m.put("payload", ev.getPayload());
        m.put("createdAt", ev.getCreatedAt().toString());
        if (ev.getActorPersonId() != null) {
            m.put("actor", people.view(people.require(ev.getActorPersonId())));
        }
        return m;
    }
}
