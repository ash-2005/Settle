# 001 — Money representation

**Decision:** `BigDecimal` + PostgreSQL `NUMERIC(19,2)`. Splits in integer paise.

**Why:** Binary floating point cannot represent 0.10 rupees. Leftover paise are assigned in sorted `person_id` order so shares always sum to the expense.

**Not:** JS `number` arithmetic on the client.
