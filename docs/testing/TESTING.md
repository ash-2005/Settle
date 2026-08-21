# Testing

## Unit tests (no database)

```powershell
docker run --rm -v "${PWD}/backend:/app" -w /app maven:3.9.9-eclipse-temurin-21 mvn -B test
```

- `MoneyTest` — ₹100 / 3 leftover paise
- `SplitCalculatorTest` — exact totals, share weights
- `SettlementPlannerTest` — nets clear with greedy pairing

## Manual demo (primary workflow)

1. `docker compose up --build`
2. Browser A: phone `9000000001`, OTP `123456`, name Ashmit
3. Create group Flatmates
4. Add member phone `9000000002` name Rahul
5. Add expense Dinner `1200`, equal, paid by Ashmit
6. Browser B (incognito): phone `9000000002`, OTP `123456`, name Rahul
7. Home should show Rahul owes ₹600
8. Group → Balances → tap through to Dinner
9. Settle tab: one recommended transfer Rahul → Ashmit ₹600

## API extras

POST `/api/expenses` with `splitMethod: EXACT|PERCENTAGE|SHARES` and the matching maps. UI is equal-only by design (fast path).
