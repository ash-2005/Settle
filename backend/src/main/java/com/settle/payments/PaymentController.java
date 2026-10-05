package com.settle.payments;

import com.settle.auth.SecurityConfig;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    public record MarkPaidRequest(UUID toPersonId, BigDecimal amount) {}

    private final PaymentService payments;

    public PaymentController(PaymentService payments) {
        this.payments = payments;
    }

    @GetMapping("/api/groups/{id}/payments")
    public List<Map<String, Object>> list(@PathVariable UUID id) {
        return payments.listGroup(SecurityConfig.currentUserId(), id);
    }

    @PostMapping("/api/groups/{id}/payments")
    public Map<String, Object> markPaid(@PathVariable UUID id, @RequestBody MarkPaidRequest body) {
        return payments.markPaid(SecurityConfig.currentUserId(), id, body.toPersonId(), body.amount());
    }

    @PostMapping("/api/payments/{id}/confirm")
    public Map<String, Object> confirm(@PathVariable UUID id) {
        return payments.resolve(SecurityConfig.currentUserId(), id, true);
    }

    @PostMapping("/api/payments/{id}/reject")
    public Map<String, Object> reject(@PathVariable UUID id) {
        return payments.resolve(SecurityConfig.currentUserId(), id, false);
    }
}
