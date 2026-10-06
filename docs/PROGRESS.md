# Progress

Running log. Newest first.

Author on GitHub: **Ashmit** (`ashmitg25@gmail.com`). No Cursor co-author.

---

## S29 — 2026-10-06

Split UI (equal / exact / percent / shares), multiple payers, expense edit with before/after activity, readable activity feed, rename in Profile, login no longer prints the OTP. Fixed API 403 for browser requests (CORS default now `*`; the proxy forwards Origin).

**Tests:** Playwright run against local stack, 8 checks pass; live API edit/audience checks.

---

## S28 — 2026-10-05

Settlement payments: payer marks paid, recipient confirms/rejects, confirmed payments reduce balances. Settle tab has "I paid this" and "Got it". One-URL prod compose + DEPLOY.md.

**Tests:** live API check (pending keeps plan, confirm clears it, non-recipient 404, double confirm 400); `mvn test`, `npm run build`.

---

## S25 — 2026-08-26

AI expense draft: `/api/ai/expense-draft`, Java parser (+ optional Gemini), speak/type on Add expense. User must confirm before save.

**Tests:** ExpenseDraftParserTest (Docker Maven when daemon up).

---

## S26 — 2026-08-26

PWA manifest, service worker, home-screen install. LAUNCH.md updated (run + share; no APK on this PC).

---

## S27 — 2026-09-18

PROJECT_STATUS + README sync (AI draft + PWA). V1 complete on GitHub.

## Local V1 — 2026-08-21

Runnable Docker stack. Dinner split verified against live API.

Unit tests: Money, SplitCalculator, SettlementPlanner, ExpenseDraftParser.

**GitHub:** https://github.com/ash-2005/Settle

---

## S01 — 2026-08-21

Project started. Human README, gitignore, commit calendar.
