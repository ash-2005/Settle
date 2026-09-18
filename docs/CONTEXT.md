# Settle — read this first (every new Cursor chat)

This file is the project brain. If this is a **new chat**, read this whole page, then [COMMIT_PLAN.md](COMMIT_PLAN.md), then [PROGRESS.md](PROGRESS.md). Do not invent a new product.

Repo: https://github.com/ash-2005/Settle  
Owner GitHub: `ash-2005`  
Git author that must appear on GitHub: **Ashmit** `<ashmitg25@gmail.com>`  
Never leave Cursor / `cursoragent@cursor.com` as author or `Co-authored-by`.

---

## Straight answers

**Is the whole app already built?**  
**Yes — V1 is on GitHub and runnable locally.** Docker: Postgres + Spring Boot + Next.js. Login, groups, expenses, balances, settlement, activity, AI draft, PWA install. **No Play Store APK.** See [CODEX_HANDOFF.md](CODEX_HANDOFF.md) for launch order (website → PWA → APK).

**How do I run it on my PC?**  
See [LAUNCH.md](LAUNCH.md). Short version: `docker compose up --build` then open `http://localhost:3000`. Local OTP is `123456`.

**How do I share with friends?**  
They cannot use `localhost` on your laptop. Either (1) they clone and run Docker themselves, (2) you put the app on the same Wi‑Fi and they use your LAN IP, or (3) you run a tunnel (Cloudflare / ngrok) to your local ports. Details in LAUNCH.md. Do not pretend UPI or bank verification works.

**How does GitHub stay “real” over weeks?**  
One slice per calendar day (or whenever Ashmit says **next**). Author date = that day. Never backdate. Never `git add .`.

---

## Next (if you are a new chat)

The **application is implemented in this workspace**. Do not scaffold a second app.

If Ashmit says to **commit a milestone** for GitHub (day-by-day):

- Stage only the files for that milestone (see SLICE_FILES.md / git status).
- Never `git add .` if that would dump unrelated WIP.
- Author Ashmit only; strip Cursor co-author before push.

If Ashmit says to **run / fix / test**, use Docker Compose and the tests in `docs/testing/TESTING.md`.

Ashmit will say `next` or `S12` or `today's commit`.

1. Read `docs/PROGRESS.md` (what landed on GitHub).
2. Read `docs/COMMIT_PLAN.md` (next unchecked slice).
3. Read `docs/SLICE_FILES.md` (exact paths to add).
4. If those files are already on disk (local app ahead of GitHub), **only stage those paths**.
5. If they are missing, implement **only that slice**, test what the slice requires, then commit.
6. Commit as Ashmit only. If Cursor injects `Co-authored-by: Cursor`, rewrite the commit with `git commit-tree` via Python (see below) **before** `git push`.
7. Push `main`. Stop. Do not start the next slice.

### Strip Cursor co-author (PowerShell)

`git commit` from Cursor often appends `Co-authored-by: Cursor <cursoragent@cursor.com>`. Author can still be Ashmit; GitHub still shows the co-author. Fix **before push**:

```python
import os, subprocess
env = os.environ.copy()
env.update({
  "GIT_AUTHOR_NAME": "Ashmit",
  "GIT_AUTHOR_EMAIL": "ashmitg25@gmail.com",
  "GIT_COMMITTER_NAME": "Ashmit",
  "GIT_COMMITTER_EMAIL": "ashmitg25@gmail.com",
})
tree = subprocess.check_output(["git", "rev-parse", "HEAD^{tree}"], text=True).strip()
parent = subprocess.check_output(["git", "rev-parse", "HEAD^"], text=True).strip()  # omit for root
msg = "the real commit message\n"
new = subprocess.check_output(["git", "commit-tree", tree, "-p", parent, "-m", msg], env=env, text=True).strip()
subprocess.check_call(["git", "update-ref", "HEAD", new])
```

Do **not** `git config` rewrite name/email. Do **not** backdate.

---

## Product (locked)

Settle is a serious shared-expense app (3rd-year CSE), not a Splitwise skin.

- Add expense in seconds: amount, description, payer(s), participants, split (default **equal**). Everything else is progressive disclosure.
- People are first-class. Groups are optional shortcuts. Pending people (phone, no account) can be on an expense; joining later attaches history.
- Contributions (who paid) ≠ obligations (who owes a share) ≠ net balance ≠ settlement plan.
- Money: Java `BigDecimal` + PostgreSQL `NUMERIC(19,2)`. Never float. INR, scale 2. Equal/percent/shares: work in paise, leftover paise to participants in stable `person_id` order.
- **Copy:** “Settle computes an optimized settlement plan that reduces unnecessary transfers using net balances.” Do **not** say it minimizes the number of payments. V1 = greedy two-heap. Exact min-transaction optimizer = later, optional, small groups.
- Cross-settlement (A→B and B→C ⇒ suggest A→C): **architecture supported**, default **OFF**, owner enables, user must confirm. Engine not in V1 code.
- Edit of approved expense → new PENDING for affected people. Creator edits → notify affected participants. Anyone else edits → notify affected participants **and** the creator. Activity example: `Rahul edited Dinner. ₹2,400 → ₹2,800. Your share: ₹600 → ₹700.`
- Delete: **affected participants only** (payers + share holders), not every group member. Unanimous affected approve → soft-delete. Creator may delete directly if group `deletion_policy = CREATOR_DIRECT` (default).
- AI never writes balances, settlements, permissions, or payment confirmation.
- Modular monolith. No microservices, Kafka, K8s.

Full design: [ARCHITECTURE.md](ARCHITECTURE.md), [DECISIONS.md](DECISIONS.md), [DATABASE.md](DATABASE.md), [API.md](API.md).

---

## Stack

- `frontend/` Next.js (App Router) + TypeScript + Tailwind
- `backend/` Java 21 (machine may have Java 22; Docker image is 21) + Spring Boot
- PostgreSQL 16
- Docker Compose
- Maven Wrapper in backend (`mvn` is not installed globally on Ashmit’s Windows)

---

## Identity model

- `users` = registered accounts (phone unique).
- `people` = anyone who can appear on an expense. Expenses FK to `people`, never to `users`.
- On register, match `people.phone` and set `user_id`. No duplicate humans.

---

## Local test accounts

Profile `local`: any phone, OTP **`123456`**. Also logged in the API container.

Do not commit real secrets. JWT secret lives in `.env` / compose env.

---

## What V1 must let Ashmit test

1. Sign up / log in with phone + OTP  
2. Add a person by phone (pending if they have no account)  
3. Create a group, add members  
4. Add a group expense (equal split, optionally multiple payers)  
5. Add an individual expense (no group)  
6. See “you owe / you are owed” and tap through to expenses  
7. Open Settle Up and see a recommended plan  
8. Leave a group without wiping old expenses  

Not required to call V1 “testable”: UPI, OCR, AI speak, disputes, recurring, cross-settlement engine.

---

## Stop rules

- Do not generate the rest of the calendar in one chat unless Ashmit explicitly wants a local test build (this file’s “testable V1”).
- Day-by-day GitHub chats: **one slice, then stop.**
- Never skip M5–M7 correctness to draw UI.
