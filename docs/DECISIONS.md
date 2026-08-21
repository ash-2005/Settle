# Decisions

## BigDecimal + NUMERIC(19,2)

Float cannot represent paise. All money is `BigDecimal` scale 2 in Java and `NUMERIC(19,2)` in Postgres. Rounding: convert to paise, integer math, leftover paise to remaining people in sorted `person_id` order so shares always sum to the expense.

## Modular monolith, not microservices

One student, one deploy, one database. Package boundaries are enough.

## `people` vs `users`

An expense participant might not have an account. The FK is always `people.id`. Registration attaches `user_id` on the matching phone.

## Contributions ≠ obligations ≠ settlement

Payers and shares are the accounting truth. Net = paid − share. A settlement **plan** is computed from nets. Paying someone does not edit old expenses.

## V1 settlement = greedy two-heap

Heap of creditors, heap of |debtors|, pair largest with largest until nets clear.

This **reduces unnecessary transfers** relative to paying every pairwise IOU. It does **not** solve “minimum number of transactions” (NP-hard). Product copy must not say “minimizes the number of payments.” A later exact optimizer may exist for tiny groups; it will not silently replace V1.

## Cross-settlement is a flag, not V3-erased

Architecture: supported. Default: OFF. Implementation: after V1 is stable. Do not delete the column or the product rule.

## Audit instead of destructive history

Soft delete, activity payloads with before/after, no silent overwrite of approved amounts.

## AI confirms with a human

AI → suggested DTO → user review → database. Never AI → database for money.

## Local OTP

Docker/dev accepts `123456` so the app is testable without SMS cost. Production will swap a provider; the identity model stays phone-first.

## GitHub cadence

Slices in COMMIT_PLAN.md. Author Ashmit only. No backdated commits. Cursor co-author stripped before push.
