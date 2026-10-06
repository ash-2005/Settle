package com.settle.users;

import com.settle.auth.SecurityConfig;
import com.settle.common.ApiException;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileController {

    public record ProfileRequest(String displayName) {}

    private final JdbcTemplate jdbc;

    public ProfileController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** The name on the account and the name everyone sees on expenses live in two rows; keep them in step. */
    @PatchMapping("/api/me")
    @Transactional
    public Map<String, Object> rename(@RequestBody ProfileRequest req) {
        String name = req.displayName() == null ? "" : req.displayName().trim();
        if (name.isEmpty() || name.length() > 60) {
            throw ApiException.bad("name must be 1 to 60 characters");
        }
        var userId = SecurityConfig.currentUserId();
        jdbc.update("UPDATE users SET display_name = ?, updated_at = now() WHERE id = ?", name, userId);
        jdbc.update("UPDATE people SET display_name = ?, updated_at = now() WHERE user_id = ?", name, userId);
        return Map.of("displayName", name);
    }
}
