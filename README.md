# Settle

A shared-expense app I'm building for my 3rd-year B.Tech CSE project.

The problem is not "people can't split a bill." The problem is everything around it: who paid what, why you suddenly owe ₹1,240, how many UPI transfers it takes to close a trip, and whether that screenshot actually means the debt is gone.

Settle is meant to make three things feel obvious:

1. Add an expense in a few seconds.
2. Tap a balance and see exactly which expenses created it.
3. Settle up from net balances, with as little back-and-forth as needed — without pretending the math is magic.

This is **not** a Splitwise clone with extra screens. Groups exist as a shortcut. You can also split with people who are not in a group, and with people who don't have a Settle account yet.

## What it does (when it's built)

- Fast add: amount, description, who paid, who shared, equal split by default
- Multiple people can pay the same bill
- Equal / exact / percentage / shares splits
- Pending people (phone number now, account later — history stays attached)
- Balances derived from expenses, not a hidden "A owes B" table
- An optimized settlement plan from net balances
- Activity that actually explains edits and payments

Later: approval/challenges, UPI/QR, payment proofs, receipts, optional AI for parsing a bill — AI never decides who owes whom.

## What it does **not** claim

Settle computes an **optimized settlement plan that reduces unnecessary transfers using net balances**.

It does **not** promise "the minimum number of payments." That problem is NP-hard. V1 uses a greedy two-heap netting algorithm. A slower exact optimizer for small groups is optional later.

Cross-settlement (A pays C instead of A→B and B→C) is in the architecture, **off by default**, and only if a group owner turns it on. Not in the first weeks of code.

## Stack

- Frontend: Next.js, TypeScript, Tailwind
- Backend: Java 21, Spring Boot
- Database: PostgreSQL
- Run locally with Docker Compose when that slice lands

One app. No microservices.

## Current status

**Slice S01 — repo only.** There is no running app yet. No login, no expenses, no Docker. This commit is the README, ignore rules, and a build log so the project can grow in small pieces.

Next: architecture and decision docs (`S02`).

## How we are building this

Small slices. Each slice is tested (when there is something to test), committed, and pushed. See [docs/PROGRESS.md](docs/PROGRESS.md) for what actually landed and [docs/COMMIT_PLAN.md](docs/COMMIT_PLAN.md) for the planned order.

## Local setup

Nothing to run yet. After `S05` you will start Postgres; after `S09` the API and web app should come up together. Instructions will live here when they are real.

## License

Personal / academic project for now. I'll pick a license when this is worth cloning.
