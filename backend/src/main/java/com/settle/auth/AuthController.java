package com.settle.auth;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @PostMapping("/auth/otp/request")
    public Map<String, String> requestOtp(@RequestBody PhoneBody body) {
        auth.requestOtp(body.phone());
        return Map.of("message", "If this were production we'd SMS you. Local code is 123456.");
    }

    @PostMapping("/auth/otp/verify")
    public Map<String, Object> verify(@RequestBody VerifyBody body) {
        return auth.verify(body.phone(), body.code(), body.displayName(), body.username());
    }

    @PostMapping("/auth/refresh")
    public Map<String, Object> refresh(@RequestBody RefreshBody body) {
        return auth.refresh(body.refreshToken());
    }

    @GetMapping("/me")
    public Map<String, Object> me() {
        return auth.me(SecurityConfig.currentUserId());
    }

    public record PhoneBody(@NotBlank String phone) {}

    public record VerifyBody(@NotBlank String phone, @NotBlank String code, String displayName, String username) {}

    public record RefreshBody(@NotBlank String refreshToken) {}
}
