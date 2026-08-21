package com.settle.people;

import com.settle.auth.SecurityConfig;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invites")
public class InviteController {

    private final InviteService invites;

    public InviteController(InviteService invites) {
        this.invites = invites;
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Body body) {
        return invites.create(SecurityConfig.currentUserId(), body.personId());
    }

    @PostMapping("/{token}/accept")
    public Map<String, Object> accept(@PathVariable String token) {
        return invites.accept(SecurityConfig.currentUserId(), token);
    }

    public record Body(UUID personId) {}
}
