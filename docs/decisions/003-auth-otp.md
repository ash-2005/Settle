# 003 — Phone OTP (mocked locally)

**Decision:** Phone is the unique identity. Local/dev OTP is a configured constant (`123456`). JWT access + refresh.

**Why:** Pending people and invite matching need phones. Paid SMS is unnecessary to demonstrate the identity model.

**Production:** swap `AuthService.requestOtp` to an SMS provider; keep the same verify path.
