package com.settle.groups;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {
    List<GroupMember> findByGroupId(UUID groupId);

    List<GroupMember> findByPersonIdAndStatus(UUID personId, String status);

    Optional<GroupMember> findByGroupIdAndPersonId(UUID groupId, UUID personId);

    List<GroupMember> findByGroupIdAndStatus(UUID groupId, String status);
}
