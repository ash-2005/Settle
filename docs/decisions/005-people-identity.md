# 005 — people vs users

**Decision:** Expenses reference `people.id`. `users` are registered accounts. Phone match on register attaches `user_id`.

**Why:** Rahul can owe money before he installs the app. History must not fork when he joins.
