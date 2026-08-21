package com.settle.people;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InviteRepository extends JpaRepository<Invite, UUID> {
    Optional<Invite> findByToken(String token);
}
