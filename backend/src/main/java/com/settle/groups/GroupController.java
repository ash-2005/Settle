package com.settle.groups;

import com.settle.auth.SecurityConfig;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groups;

    public GroupController(GroupService groups) {
        this.groups = groups;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return groups.listMine(SecurityConfig.currentUserId());
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody CreateBody body) {
        return groups.create(SecurityConfig.currentUserId(), body.name(), body.type());
    }

    @GetMapping("/{id}")
    public Map<String, Object> one(@PathVariable UUID id) {
        return groups.view(SecurityConfig.currentUserId(), id);
    }

    @PostMapping("/{id}/members")
    public Map<String, Object> add(@PathVariable UUID id, @RequestBody MemberBody body) {
        return groups.addMember(SecurityConfig.currentUserId(), id, body.personId());
    }

    @PostMapping("/{id}/leave")
    public Map<String, Object> leave(@PathVariable UUID id) {
        return groups.leave(SecurityConfig.currentUserId(), id);
    }

    @PostMapping("/{id}/members/{personId}/remove")
    public Map<String, Object> remove(@PathVariable UUID id, @PathVariable UUID personId) {
        return groups.removeMember(SecurityConfig.currentUserId(), id, personId);
    }

    public record CreateBody(@NotBlank String name, String type) {}

    public record MemberBody(UUID personId) {}
}
