# Project status

**Runnable locally** with Docker Compose: auth, people, groups, expenses, balances, settlement plan, activity, soft-delete.

**GitHub** may lag local files if commits are landed day-by-day. This file describes **what the code in this workspace does**.

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
- Next.js UI for the main loop
- Flyway schema, Docker Compose, unit tests for money/splits/settlement

## Architecture

Modular monolith. Browser → Next.js → Spring Boot → PostgreSQL. No LLM on balances.

## Tests

Unit tests in `backend/src/test`. Run via Maven in Docker (no global `mvn` on the Windows machine).

## Known issues / limitations

- UI add-expense is **equal split, one payer**. Other split methods and multi-payer are implemented in the API.
- OTP is a local constant, not SMS
- JWT in `localStorage` (fine for a student demo, not a bank)
- No UPI, OCR, AI parse, disputes, or recurring generation
- Cross-settlement flag exists on groups, engine not implemented (default OFF)
- Owner cannot leave without a future ownership-transfer feature
- Invite accept does not merge two person rows; register-with-same-phone is the merge

## Deployment

Docker Compose is the supported path. Production would replace OTP, JWT secret, and CORS origins. Not deployed to a public host in this repo.

## Future

Disputes, notifications, recurring, UPI/QR + proof, receipt OCR, optional exact settlement optimizer for tiny groups, cross-settlement engine when the owner enables it.
