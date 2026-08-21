# 004 — Settlement algorithm honesty

**Decision:** V1 uses greedy two-heap netting. Product copy: “optimized settlement plan that reduces unnecessary transfers using net balances.”

**Why:** Minimizing the *count* of payments is NP-hard. Claiming “minimum payments” would be false.

**Later:** optional exact optimizer for tiny n; cross-settlement engine behind an OFF-by-default group flag.
