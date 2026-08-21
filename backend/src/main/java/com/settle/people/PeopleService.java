package com.settle.people;

import com.settle.common.ApiException;
import com.settle.common.Phones;
import com.settle.users.UserAccountRepository;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PeopleService {

    private final PersonRepository people;
    private final UserAccountRepository users;

    public PeopleService(PersonRepository people, UserAccountRepository users) {
        this.people = people;
        this.users = users;
    }

    public Person require(UUID id) {
        return people.findById(id).orElseThrow(ApiException::notFound);
    }

    public Person requireForUser(UUID userId) {
        return people.findByUserId(userId).orElseThrow(ApiException::notFound);
    }

    @Transactional
    public Person addByPhone(UUID actorUserId, String rawPhone, String name) {
        String phone = Phones.normalize(rawPhone);
        String display = (name == null || name.isBlank()) ? phone : name.trim();
        return people.findByPhone(phone).orElseGet(() ->
                people.save(Person.pending(phone, display, actorUserId)));
    }

    public Map<String, Object> view(Person p) {
        boolean pending = p.getUserId() == null;
        return Map.of(
                "id", p.getId(),
                "phone", p.getPhone() == null ? "" : p.getPhone(),
                "displayName", p.getDisplayName(),
                "username", p.getUserId() == null ? "" : users.findById(p.getUserId()).map(u -> u.getUsername()).orElse(""),
                "pending", pending);
    }
}
