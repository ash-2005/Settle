# Codex handoff — Settle (website first, then app)

Paste this (or point Codex at this file) when continuing work on launch, hosting, or APK.

## One-line pitch

**Settle** is a shared-expense app: add a bill in seconds, see exactly why you owe money, settle from net balances. Java owns all money math; AI only suggests a draft the user confirms.

## Repo

- GitHub: https://github.com/ash-2005/Settle
- Stack: Next.js 15 + Spring Boot 3.3 + PostgreSQL 16 + Docker Compose
- Author on commits: Ashmit `<ashmitg25@gmail.com>` — never Cursor co-author

## What is built (V1 — runnable today)

### Backend (`backend/`, Java `com.settle`)

| Area | Status |
|------|--------|
| Phone OTP auth + JWT | Mock OTP `123456`, logs code; pluggable SMS later |
| `users` + `people` | Pending participants by phone; expenses FK to `people` |
| Invites | Create/accept token links |
| Groups | Create, members, leave, owner remove |
| Expenses | Group or individual; multi-payer; EQUAL/EXACT/PERCENT/SHARES |
| Money | BigDecimal + NUMERIC; paise rounding |
| Balances | Derived on read; “why I owe” breakdown |
| Settlement | Greedy two-heap planner (honest: reduces transfers, not “min payments”) |
| Activity | Events + audience rows |
| AI draft | `POST /api/ai/expense-draft` — parser default; optional Gemini if `GEMINI_API_KEY` set. **Never auto-saves.** |
| DB | Flyway V1 schema + V2 activity payload fix |

### Frontend (`frontend/`, Next.js App Router)

| Screen | Status |
|--------|--------|
| Login | Phone + OTP |
| Home | You owe / you are owed, groups, activity |
| Groups | List, create, detail (Overview / Expenses / Balances / Settle) |
| Add expense | Equal split; type/speak → AI fill → user confirms |
| Expense detail | Payers, shares, soft-delete |
| Activity, Profile | Working |
| PWA | `manifest.ts`, service worker — **Add to Home screen** on Android (not Play Store APK) |

### Tests (backend)

- `MoneyTest`, `SplitCalculatorTest`, `SettlementPlannerTest`, `ExpenseDraftParserTest`
- Run: `docker run --rm -v "${PWD}/backend:/app" -w /app maven:3.9.9-eclipse-temurin-21 mvn -B test`

## What is NOT built

- Play Store **APK** (no Android SDK on dev machine; no Capacitor/TWA project yet)
- Real SMS OTP (MSG91/Twilio)
- UPI / QR / payment proofs / receipt OCR
- Disputes, approvals, recurring expenses, cross-settlement engine (flag exists, default OFF)
- Public production deploy (no VPS/Railway config committed)

## Launch order (website → app)

### Phase 1 — Website (do this first)

1. Host API + Postgres + Next.js (VPS, Railway, Render, or Fly.io — Docker Compose is ready).
2. Set env: `JWT_SECRET`, `CORS_ORIGINS`, `NEXT_PUBLIC_API_URL` (public HTTPS API URL).
3. Optional: `GEMINI_API_KEY` for smarter expense drafts.
4. Replace mock OTP with SMS provider behind same `/api/auth/otp/*` routes.
5. Friends open **https://your-domain.com** in Chrome.

### Phase 2 — “App” without Play Store

- PWA already works: Chrome → **Add to Home screen**.
- Requires Phase 1 public HTTPS URL (not `localhost`).

### Phase 3 — Native APK (later)

1. Install Android Studio on a machine with SDK.
2. Wrap the same web app (Capacitor or Trusted Web Activity).
3. Point `NEXT_PUBLIC_API_URL` / Capacitor config at **production API URL**.
4. Build signed APK/AAB.
5. **Critical:** APK alone is useless if API is still localhost.

### Sharing with friends today (dev)

- **Same Wi‑Fi:** PC runs `docker compose up`, phone opens `http://<LAN-IP>:3000`, rebuild frontend with `NEXT_PUBLIC_API_URL=http://<LAN-IP>:8080`.
- **Tunnel:** ngrok/Cloudflare on ports 3000 + 8080, rebuild frontend with public API URL.
- **Clone:** Friend runs Docker from GitHub (needs Docker installed).

## Run locally (Ashmit’s Windows PC)

```powershell
cd "C:\Users\Ashmit\Desktop\ai repo"
docker compose up --build
```

- Web: http://localhost:3000
- API: http://localhost:8080/api/health
- OTP: `123456`

## Product rules (do not break)

- Settlement copy: “optimized plan that reduces unnecessary transfers” — **never** “minimizes number of payments.”
- AI → suggested draft → **user review** → database. AI never writes balances or confirms payments.
- Cross-settlement: supported in architecture, default OFF.
- Expenses FK to `people`, not `users`. Money never float.

## Git history (main milestones)

1. S01 — README + repo skeleton
2. Spring Boot API (auth, groups, expenses, balances, settlement)
3. Next.js UI (login through settle-up)
4. Docker Compose
5. Docs (architecture, database, API, decisions, milestones)
6. AI expense draft + speak-to-fill
7. PWA manifest for phone home screen

## Suggested next tasks for Codex

1. Deploy to a public host (Railway/Render/VPS) with HTTPS.
2. Wire real SMS OTP.
3. Add Capacitor project → build debug APK pointing at production API.
4. Multi-payer + exact/percent/shares in Add expense UI (API already supports).
5. UPI deep links (V2 spec — no fake bank verification).

## Key files

- [CONTEXT.md](CONTEXT.md) — agent rules
- [LAUNCH.md](LAUNCH.md) — run + share
- [PROJECT_STATUS.md](PROJECT_STATUS.md) — current state
- [ARCHITECTURE.md](ARCHITECTURE.md) — design
- [docker-compose.yml](../docker-compose.yml) — local stack
