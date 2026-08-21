# Architecture

```mermaid
flowchart LR
  Browser --> NextApp
  NextApp --> SpringApi
  SpringApi --> Postgres
```

## Components

- **frontend/** — Next.js App Router. Login, home, groups, add expense, expense detail, activity, profile. Calls `/api/*` with a JWT from `localStorage`.
- **backend/** — Spring Boot modular monolith (`com.settle.auth|users|people|groups|expenses|balances|settlements|activity`).
- **postgres** — source of truth. Flyway in `backend/src/main/resources/db/migration`.

## Data flow (add expense)

1. User submits amount, description, payers, participants, split method.
2. One DB transaction: expense row, payer rows, participant shares (paise-rounded), activity + audience.
3. Balances are **not stored**. They are `sum(paid) - sum(share)` over ACTIVE expenses.
4. Settle Up runs `SettlementPlanner` on those nets and returns suggested transfers. Paying later (UPI) is out of V1.

## Security boundary

Unauthenticated: `/api/health`, `/api/auth/**`. Everything else: Bearer JWT. Group/expense APIs 404 if you are not a member/participant. CORS allowlist. Secrets in environment.

## Design decisions

See [docs/decisions](../decisions/). Cross-settlement is **supported in schema**, default OFF, no engine yet. AI is not on this path.

## Scale

Indexes on phones, membership, expense FKs. Pagination is the next step if a group has thousands of expenses. Redis is not required.
