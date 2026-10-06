package com.settle.friends;

import com.settle.auth.SecurityConfig;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FriendController {

    private final FriendService friends;

    public FriendController(FriendService friends) {
        this.friends = friends;
    }

    @GetMapping("/api/friends")
    public List<Map<String, Object>> list() {
        return friends.friends(SecurityConfig.currentUserId());
    }

    @GetMapping("/api/friends/{personId}")
    public Map<String, Object> one(@PathVariable UUID personId) {
        return friends.friend(SecurityConfig.currentUserId(), personId);
    }
}
