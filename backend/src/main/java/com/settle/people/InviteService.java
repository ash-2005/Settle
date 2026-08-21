package com.settle.people;

import com.settle.common.ApiException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InviteService {

    private final InviteRepository invites;
    private final PersonRepository people;
    private final PeopleService peopleService;

    public InviteService(InviteRepository invites, PersonRepository people, PeopleService peopleService) {
        this.invites = invites;
        this.people = people;
        this.peopleService = peopleService;
    }

    @Transactional
    public Map<String, Object> create(UUID userId, UUID personId) {
        Person target = people.findById(personId).orElseThrow(ApiException::notFound);
        Invite invite = invites.save(Invite.create(target.getId(), userId));
        return Map.of(
                "token", invite.getToken(),
                "person", peopleService.view(target),
                "expiresAt", invite.getExpiresAt().toString());
    }

    /**
     * Phone-first identity: registering with the pending phone already attaches the person.
     * Accepting the link just confirms that and closes the invite.
     */
    @Transactional
    public Map<String, Object> accept(UUID userId, String token) {
        Invite invite = invites.findByToken(token).orElseThrow(ApiException::notFound);
        if (invite.getAcceptedAt() != null) {
            throw ApiException.bad("Invite already used");
        }
        if (invite.getExpiresAt().isBefore(Instant.now())) {
            throw ApiException.bad("Invite expired");
        }
        Person me = people.findByUserId(userId).orElseThrow(ApiException::notFound);
        Person pending = people.findById(invite.getPersonId()).orElseThrow(ApiException::notFound);
        boolean samePerson = pending.getId().equals(me.getId());
        boolean samePhone = pending.getPhone() != null && pending.getPhone().equals(me.getPhone());
        boolean alreadyAttached = userId.equals(pending.getUserId());
        if (!samePerson && !samePhone && !alreadyAttached) {
            throw ApiException.bad("Sign in with the phone this invite was created for");
        }
        invite.accept();
        invites.save(invite);
        return Map.of("ok", true, "person", peopleService.view(me));
    }
}
