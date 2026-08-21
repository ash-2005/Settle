# Database

PostgreSQL 16. Migrations: Flyway, `backend/src/main/resources/db/migration/`.

Money: `NUMERIC(19,2)`. Currency default `INR`.

## users

- `id` UUID PK
- `phone` VARCHAR unique not null (E.164-ish, digits with country code)
- `username` VARCHAR unique not null
- `display_name` VARCHAR not null
- `created_at`, `updated_at`

## people

- `id` UUID PK
- `phone` VARCHAR unique nullable
- `user_id` UUID unique nullable FK users
- `display_name` VARCHAR not null
- `created_by_user_id` UUID nullable FK users
- `created_at`, `updated_at`

## invites

- `id` UUID PK
- `person_id` FK people
- `token` VARCHAR unique
- `created_by_user_id` FK users
- `expires_at`, `accepted_at`

## groups

- `id` UUID PK
- `name` VARCHAR
- `type` VARCHAR (TRIP, FLATMATES, FRIENDS, FAMILY, COLLEGE, CUSTOM)
- `owner_user_id` FK users
- `approval_mode` VARCHAR default `OFF`
- `approval_threshold` NUMERIC(19,2) nullable
- `allow_cross_settlement` BOOLEAN default false
- `deletion_policy` VARCHAR default `CREATOR_DIRECT` (`AFFECTED_CONSENSUS_ONLY` other value)
- timestamps

## group_members

- `group_id` FK, `person_id` FK
- `role` OWNER | MEMBER
- `status` ACTIVE | LEFT | REMOVED
- `left_at` nullable
- unique (group_id, person_id)

## expenses

- `id` UUID PK
- `group_id` UUID nullable FK groups
- `created_by_user_id` FK users
- `description` VARCHAR
- `amount` NUMERIC(19,2) check > 0
- `currency` CHAR(3) default INR
- `category` VARCHAR nullable
- `split_method` EQUAL | EXACT | PERCENTAGE | SHARES | ITEM_WISE
- `status` ACTIVE | UNDER_DISPUTE | DELETED
- `approval_status` VARCHAR
- `expense_date` date
- timestamps

## expense_payers

- `expense_id`, `person_id`, `amount` > 0
- unique (expense_id, person_id)
- sum(amount) must equal expenses.amount (enforced in app transaction)

## expense_participants

- `expense_id`, `person_id`, `share_amount`
- `share_count` nullable, `percentage` nullable
- unique (expense_id, person_id)
- sum(share_amount) = expenses.amount (app)

## expense_deletion_requests / votes (when delete-consensus ships)

- request: expense_id, initiated_by person_id, status
- votes: request_id, person_id, approve boolean, unique voter

## settlements / settlement_payments (when pay-in-app ships)

- from_person, to_person, group_id nullable, amount, remaining_amount, status PENDING | PARTIALLY_PAID | SETTLED

## activity_events

- actor_person_id, action, entity_type, entity_id, audience_scope, payload JSONB, created_at

## activity_audience

- event_id, person_id

## Indexes (V1)

people(phone), users(phone), group_members(group_id, status), expenses(group_id), expense_payers(person_id), expense_participants(person_id), activity(entity_type, entity_id, created_at).

## Net balance (not a table)

For person P in a scope:

`sum(payer.amount) - sum(participant.share_amount)` over ACTIVE expenses in that group (or pairwise non-group expenses).
