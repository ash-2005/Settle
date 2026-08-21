# Architecture

Modular monolith. One Spring Boot API, one Next.js app, one PostgreSQL. Docker Compose locally.

```
Browser  →  Next.js :3000  →  Spring Boot :8080  →  Postgres :5432
```

No LLM on the money path. AI (later) may suggest an expense; the user confirms; Java writes the row.

## Frontend

`frontend/app` — App Router. Bottom nav: Home, Groups, Activity, Profile. FAB Add.

Features under `frontend/features/*`. TanStack Query. Display money as strings from the API; never add rupees with JS `number`.

## Backend packages (`com.settle`)

`auth`, `users`, `people`, `groups`, `expenses`, `balances`, `settlements`, `payments`, `disputes`, `notifications`, `activity`, `recurring`, `receipts`, `ai`, plus `common`.

V1 implements auth → people → groups → expenses → balances → settlement plan → basic activity. Other packages may exist as empty or stub.

## Expense lifecycle

Create (payers + participants + shares in one transaction) → activity → balances **derived** on read → Settle Up **recommends** transfers → (later) payment rows → recipient confirm.

Settlement never rewrites expense history.

## Cross-settlement

**Supported in this architecture.** Column `groups.allow_cross_settlement` default false. Owner may enable. Engine that suggests A→C instead of A→B plus B→C is **not V1**. User must confirm any such suggestion when it exists.

## Notification audience (edits)

- Creator edits → affected participants (payers + share holders), not the editor.
- Anyone else edits → those people **and** the creator (even if the creator’s share did not change).
- Uninvolved group members are out.

Activity text: actor, description, amount before→after, viewer share before→after.

## Deletion

Soft-delete only (`status = DELETED`). Affected participants = payers ∪ share holders. Uninvolved members cannot delete. Unanimous affected approval of a deletion request → DELETED. Creator may direct-delete when `deletion_policy = CREATOR_DIRECT`.
