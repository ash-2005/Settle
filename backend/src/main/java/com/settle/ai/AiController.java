package com.settle.ai;

import com.settle.auth.SecurityConfig;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiDraftService ai;

    public AiController(AiDraftService ai) {
        this.ai = ai;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return ai.status();
    }

    @PostMapping("/expense-draft")
    public Map<String, Object> draft(@RequestBody DraftBody body) {
        return ai.draft(SecurityConfig.currentUserId(), body.text(), body.groupId());
    }

    public record DraftBody(@NotBlank String text, UUID groupId) {}
}
