# Settle

Shared expenses for groups of people who actually want to know **why** they owe money, then settle with a small number of transfers — not a mystery running total.

This is a 3rd-year B.Tech CSE project: Java does every rupee of arithmetic. The UI stays simple.

## Problem

Splitting a trip or a flat usually fails after the first bill:

- Adding an expense takes too long
- Nobody can explain a balance without a screenshot thread
- Everyone pays everyone, instead of netting first
- Friends who are not “in the app yet” get left out of the math

## Solution

1. Add an expense in a few fields (amount, what, who paid, who shared, equal split).
2. Open a balance and see the expenses that created it.
3. Settle from **net balances**. Settle computes an **optimized settlement plan that reduces unnecessary transfers**. It does **not** claim the mathematically minimum number of payments (that problem is NP-hard).

People are first-class. Groups are optional. Someone without an account can still be on an expense (phone / invite); when they join, history stays on the same person record.

## What works today

- Phone + OTP login (local code **123456**, no SMS)
- Add people by phone (pending until they register)
- Groups, members, leave / remove
- Group and individual expenses
- Multiple payers (API); equal split in the UI
- Exact / percentage / shares splits (API + unit tests)
- Derived balances (not a stored “A owes B” table)
- Greedy two-heap settlement plan
- Activity feed
- Soft-delete expenses
- Docker Compose for Postgres + API + web

Not in this build (on purpose): UPI/QR, payment-proof OCR, AI bill parsing, dispute workflows, recurring expenses. Architecture leaves room; AI will never own the money path.

## Stack

- Next.js 15, TypeScript, Tailwind
- Java 21, Spring Boot 3.3
- PostgreSQL 16, Flyway
- Docker Compose

## Run

Docker Desktop must be running.

```powershell
docker compose up --build
```

- App: http://localhost:3000
- API: http://localhost:8080/api/health

Sign in with any 10-digit Indian number. OTP is `123456`.

Use two browsers (or a phone + laptop) with two numbers to demo a real split.

Stop: `docker compose down`  
Wipe DB: `docker compose down -v`

More: [docs/setup/SETUP.md](docs/setup/SETUP.md) and [docs/LAUNCH.md](docs/LAUNCH.md) (sharing with friends).

## Tests

```powershell
docker run --rm -v "${PWD}/backend:/app" -w /app maven:3.9.9-eclipse-temurin-21 mvn -B test
```

Covers money rounding, split math, and the settlement planner.

## Environment

See [.env.example](.env.example). Do not commit `.env`. Local JWT secret is a dummy string for Docker.

## Architecture

[docs/architecture/ARCHITECTURE.md](docs/architecture/ARCHITECTURE.md) · [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md)

## License

Academic / personal project.
