package com.settle.groups;

import com.settle.common.ApiException;
import com.settle.people.PeopleService;
import com.settle.people.Person;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GroupService {

    private final ExpenseGroupRepository groups;
    private final GroupMemberRepository members;
    private final PeopleService people;

    public GroupService(ExpenseGroupRepository groups, GroupMemberRepository members, PeopleService people) {
        this.groups = groups;
        this.members = members;
        this.people = people;
    }

    @Transactional
    public Map<String, Object> create(UUID userId, String name, String type) {
        if (name == null || name.isBlank()) {
            throw ApiException.bad("group name required");
        }
        Person me = people.requireForUser(userId);
        ExpenseGroup group = groups.save(ExpenseGroup.create(name.trim(), type, userId));
        members.save(GroupMember.join(group.getId(), me.getId(), "OWNER"));
        return view(userId, group.getId());
    }

    public List<Map<String, Object>> listMine(UUID userId) {
        Person me = people.requireForUser(userId);
        List<Map<String, Object>> out = new ArrayList<>();
        for (GroupMember m : members.findByPersonIdAndStatus(me.getId(), "ACTIVE")) {
            groups.findById(m.getGroupId()).ifPresent(g -> out.add(summary(g)));
        }
        return out;
    }

    public Map<String, Object> view(UUID userId, UUID groupId) {
        ExpenseGroup g = visibleGroup(userId, groupId);
        List<Map<String, Object>> memberViews = new ArrayList<>();
        for (GroupMember m : members.findByGroupId(g.getId())) {
            Person p = people.require(m.getPersonId());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("person", people.view(p));
            row.put("role", m.getRole());
            row.put("status", m.getStatus());
            memberViews.add(row);
        }
        Map<String, Object> body = new LinkedHashMap<>(summary(g));
        body.put("members", memberViews);
        body.put("allowCrossSettlement", g.isAllowCrossSettlement());
        body.put("deletionPolicy", g.getDeletionPolicy());
        return body;
    }

    @Transactional
    public Map<String, Object> addMember(UUID userId, UUID groupId, UUID personId) {
        ExpenseGroup g = requireOwner(userId, groupId);
        people.require(personId);
        var existing = members.findByGroupIdAndPersonId(groupId, personId);
        if (existing.isPresent()) {
            GroupMember m = existing.get();
            if ("ACTIVE".equals(m.getStatus())) {
                throw ApiException.bad("already a member");
            }
            members.delete(m);
        }
        members.save(GroupMember.join(g.getId(), personId, "MEMBER"));
        return view(userId, groupId);
    }

    @Transactional
    public Map<String, Object> leave(UUID userId, UUID groupId) {
        Person me = people.requireForUser(userId);
        ExpenseGroup g = groups.findById(groupId).orElseThrow(ApiException::notFound);
        if (g.getOwnerUserId().equals(userId)) {
            throw ApiException.bad("transfer ownership before leaving (not in this slice — stay, or remove the group later)");
        }
        GroupMember m = members.findByGroupIdAndPersonId(groupId, me.getId()).orElseThrow(ApiException::notFound);
        if (!"ACTIVE".equals(m.getStatus())) {
            throw ApiException.bad("already left");
        }
        m.markLeft("LEFT");
        members.save(m);
        return Map.of("ok", true);
    }

    @Transactional
    public Map<String, Object> removeMember(UUID userId, UUID groupId, UUID personId) {
        requireOwner(userId, groupId);
        GroupMember m = members.findByGroupIdAndPersonId(groupId, personId).orElseThrow(ApiException::notFound);
        if ("OWNER".equals(m.getRole())) {
            throw ApiException.bad("cannot remove the owner");
        }
        m.markLeft("REMOVED");
        members.save(m);
        return view(userId, groupId);
    }

    public ExpenseGroup visibleGroup(UUID userId, UUID groupId) {
        Person me = people.requireForUser(userId);
        ExpenseGroup g = groups.findById(groupId).orElseThrow(ApiException::notFound);
        members.findByGroupIdAndPersonId(groupId, me.getId()).orElseThrow(ApiException::notFound);
        return g;
    }

    public List<GroupMember> activeMembers(UUID groupId) {
        return members.findByGroupIdAndStatus(groupId, "ACTIVE");
    }

    public boolean isActiveMember(UUID groupId, UUID personId) {
        return members.findByGroupIdAndPersonId(groupId, personId)
                .filter(m -> "ACTIVE".equals(m.getStatus()))
                .isPresent();
    }

    private ExpenseGroup requireOwner(UUID userId, UUID groupId) {
        ExpenseGroup g = visibleGroup(userId, groupId);
        if (!g.getOwnerUserId().equals(userId)) {
            throw ApiException.notFound();
        }
        return g;
    }

    private Map<String, Object> summary(ExpenseGroup g) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", g.getId());
        m.put("name", g.getName());
        m.put("type", g.getType());
        m.put("ownerUserId", g.getOwnerUserId());
        return m;
    }
}
