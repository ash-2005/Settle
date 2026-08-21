# Database

Canonical schema: [../DATABASE.md](../DATABASE.md)

Migrations: `backend/src/main/resources/db/migration/V1__init.sql`

Balances are not a table. Nets are computed from `expense_payers` and `expense_participants`.
