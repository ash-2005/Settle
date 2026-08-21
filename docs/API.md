# API

Base `/api`. JSON. Bearer JWT after OTP verify (except auth + health).

Unauthorized private resources: prefer **404** over 403 so IDs do not leak.

## Health

- `GET /api/health` → `{ "status": "ok" }`

## Auth

- `POST /api/auth/otp/request` `{ "phone": "+91..." }`
- `POST /api/auth/otp/verify` `{ "phone", "code", "displayName?", "username?" }` → `{ accessToken, refreshToken, user }`
- `POST /api/auth/refresh` `{ "refreshToken" }`
- `GET /api/me`

Local Docker: code `123456`.

## People / invites

- `POST /api/people/phone` `{ "phone", "name" }` — match or create pending person
- `POST /api/invites` `{ "personId" }`
- `POST /api/invites/{token}/accept`

## Groups

- `GET /api/groups` `POST /api/groups` `{ name, type }`
- `GET /api/groups/{id}` `PATCH /api/groups/{id}`
- `POST /api/groups/{id}/members` `{ personId }`
- `POST /api/groups/{id}/leave`
- `POST /api/groups/{id}/members/{personId}/remove`

## Expenses

- `POST /api/expenses` amount, description, groupId?, payers[], participantIds, splitMethod, exact/percent/shares as needed
- `GET /api/expenses/{id}`
- `GET /api/groups/{id}/expenses`
- `GET /api/people/{id}/expenses`
- `PATCH /api/expenses/{id}`
- `POST /api/expenses/{id}/delete` (creator, if policy allows)
- deletion-request endpoints when that slice lands

## Balances / settlement plan

- `GET /api/groups/{id}/balances`
- `GET /api/people/{id}/balances`
- `GET /api/groups/{id}/settlement-plan` computed, not persisted

Plan items: `{ fromPersonId, toPersonId, amount }`. Copy in clients: optimized plan from nets, not “minimum payments.”

## Activity

- `GET /api/groups/{id}/activity`
- `GET /api/activity`
