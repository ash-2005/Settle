# Commit plan

One slice per commit unless Ashmit says otherwise. Say **next** when the next slice should land.

Check a box only after that slice is on GitHub.

Commits must show **Ashmit** as author and committer. No `Co-authored-by: Cursor`.

---

## Week 1 — paper + skeleton (M0 / M1)

- [x] **S01** README, `.gitignore`, folder placeholders, PROGRESS.md, COMMIT_PLAN.md. Create public GitHub repo. Test: `git status`.
- [x] **S02** `docs/ARCHITECTURE.md` + `docs/DECISIONS.md` (landed in docs commit).
- [x] **S03** `docs/DATABASE.md` (landed in docs commit).
- [x] **S04** `docs/API.md` + `docs/DEVELOPMENT.md` (landed in docs commit).
- [x] **S05–S09** Docker + Spring + Next (landed in foundation commits).
- [x] **S10–S24** Auth through settlement (landed in API + frontend commits).
- [x] **S25** AI expense draft: type/speak → review → save (not auto-post).
- [ ] **S26** PWA: manifest + home-screen install for phone demo.

## Week 2 — identity and groups (M2–M4)

- [ ] **S10** Flyway/Liquibase + `users` / `people` tables.
- [ ] **S11** Request OTP (mock: log code / fixed dev code).
- [ ] **S12** Verify OTP + JWT issue/refresh.
- [ ] **S13** `GET /api/me` + profile username/display name.
- [ ] **S14** Add person by phone (match or pending).
- [ ] **S15** Invite links create/accept.
- [ ] **S16** Create/list groups + owner.
- [ ] **S17** Add members, leave, remove (membership status, not hard delete).

## Week 3 — expenses and money (M5–M7)

- [ ] **S18** Create expense, equal split, one payer, group context. Tests for split math.
- [ ] **S19** Multiple payers + exact / percentage / shares validation.
- [ ] **S20** Individual expense (`group_id` null) + list by person.
- [ ] **S21** Derived balances + "why I owe" breakdown API.
- [ ] **S22** Settlement algorithm in Java + unit tests (greedy two-heap; do not claim min payment count).
- [ ] **S23** Settlement-plan API + simple Settle Up page.
- [ ] **S24** Soft-delete expense + activity event on create/edit.

## Week 4 — activity, hardening, honest README

- [ ] **S25** Activity feed (group + home), audience filter if cheap.
- [ ] **S26** Leave/remove with outstanding balance still payable (individual).
- [ ] **S27** Scenario tests (simple dinner, multi-payer, pending person stub).
- [ ] **S28** README status update, run instructions that match compose, limitations.
- [ ] **S29** Security pass: authz on existing endpoints, no secrets in git.
- [ ] **S30** Buffer / bugfix day.

## After V1 is stable (new calendar)

Disputes, notifications, recurring, UPI, proofs, OCR/AI, **cross-settlement engine** (flag already in architecture, default OFF), optional exact settlement optimizer for small groups.
