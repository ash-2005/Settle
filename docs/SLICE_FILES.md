# Files to stage per slice

Use this instead of `git add .`. If a path is missing, implement it in that slice. If it already exists locally, still add **only** these paths.

## S01 (done)

- `.gitignore`
- `README.md`
- `docs/PROGRESS.md`
- `docs/COMMIT_PLAN.md`
- `backend/.gitkeep`
- `frontend/.gitkeep`

## S02

- `docs/ARCHITECTURE.md`
- `docs/DECISIONS.md`

## S03

- `docs/DATABASE.md`

## S04

- `docs/API.md`
- `docs/DEVELOPMENT.md`

## S05

- `docker-compose.yml` (Postgres service only is enough for this slice; extra services can wait until S09)
- `.env.example`

## S06

- `backend/pom.xml`
- `backend/mvnw`, `backend/mvnw.cmd`, `backend/.mvn/**`
- `backend/Dockerfile`
- `backend/src/main/java/com/settle/SettleApplication.java`
- `backend/src/main/java/com/settle/health/**`
- `backend/src/main/resources/application.yml`

## S07

- `backend/src/main/java/com/settle/common/Money.java`
- `backend/src/main/java/com/settle/common/MoneyRounding.java`
- `backend/src/test/java/com/settle/common/**`

## S08

- `frontend/package.json`, `frontend/package-lock.json` (or `pnpm-lock.yaml`)
- `frontend/tsconfig.json`, `frontend/next.config.ts` (or `.mjs`)
- `frontend/tailwind` / `postcss` config
- `frontend/app/**` shell (home, bottom nav), no real API yet
- `frontend/Dockerfile` if created

## S09

- `docker-compose.yml` api + web services
- `frontend` health/status fetch to `/api/health`
- `backend` CORS for the web origin

## S10

- `backend/src/main/resources/db/migration/V1*.sql` (users, people)
- JPA entities + repositories for users/people

## S11–S13 (auth)

- `backend/.../auth/**`
- security config, JWT, OTP (local code 123456)
- `frontend` login / OTP screens
- `GET /api/me`

## S14–S15 (people + invites)

- people-by-phone API
- invite entity + accept
- `frontend` add-person UI

## S16–S17 (groups)

- groups + group_members
- leave/remove
- `frontend` groups list + create + members

## S18–S20 (expenses)

- expenses, payers, participants
- split math + tests
- create expense UI (equal default; exact/percent/shares behind customize)
- individual expenses (`group_id` null)

## S21–S23 (balances + settlement)

- balance service (derived, not stored as truth)
- `SettlementPlanner` greedy two-heap + tests
- settle-up page
- copy: reduces unnecessary transfers; not min payment count

## S24–S26 (activity + leave)

- activity_events
- soft delete
- leave with outstanding individual settlement

## S27–S30

- scenario tests, README status, security pass, buffer

## Handoff docs (not a numbered slice; may land with S02)

These help new Cursor chats. Stage them when Ashmit wants them on GitHub:

- `docs/CONTEXT.md`
- `docs/LAUNCH.md`
- `docs/SLICE_FILES.md`
- `AGENTS.md`
- `.cursor/rules/settle.mdc`
