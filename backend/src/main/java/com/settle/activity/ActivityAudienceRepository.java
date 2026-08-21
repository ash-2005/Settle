package com.settle.activity;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityAudienceRepository extends JpaRepository<ActivityAudience, ActivityAudience.PK> {
    List<ActivityAudience> findByPersonId(UUID personId);
}
