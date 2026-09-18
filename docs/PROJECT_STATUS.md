# Project status

**Last updated:** 2026-09-18. Runnable locally with Docker Compose. **On GitHub:** full V1 + AI draft + PWA.

## Completed

- Phone identity + mock OTP (`123456`) + JWT
- `people` vs `users` (pending participants)
- Invite tokens (accept with matching phone)
- Groups, add member by phone, leave, owner remove
- Expenses: group or individual, multiple payers, EQUAL / EXACT / PERCENTAGE / SHARES
- Money in `BigDecimal` / `NUMERIC(19,2)`, paise rounding
- Derived balances + “why I owe” lines
- Greedy two-heap settlement plan (honest copy: not min-payment-count)
- Activity events with audience rows
- Soft-delete by creator
- Next.js UI: login, home, groups, add expense, balances, settle-up, activity, profile
- **AI expense draft:** `/api/ai/expense-draft` — Java parser; optional Gemini; user must confirm
- **PWA:** manifest + service worker — install via Chrome “Add to Home screen”
- Flyway schema, Docker Compose, unit tests

## Architecture

Modular monolith. Browser → Next.js → Spring Boot → PostgreSQL. No LLM on balances.

## Launch path

1. **Website first** — deploy Docker stack to a public host with HTTPS
2. **PWA** — already works once site is public
3. **APK later** — Capacitor/TWA + Android Studio; must point at public API, not localhost

See [CODEX_HANDOFF.md](CODEX_HANDOFF.md) and [LAUNCH.md](LAUNCH.md).

## Tests

Unit tests in `backend/src/test`. Run via Maven in Docker when Docker Desktop is up.

## Known limitations

- UI add-expense: equal split, one payer (API supports more)
- OTP is local constant, not SMS
- No Play Store APK in repo
- No UPI, disputes, recurring, cross-settlement engine (flag OFF)
- Not deployed to production yet

## Future

Public deploy, real SMS, Capacitor APK, UPI/QR, disputes, recurring, cross-settlement engine.
