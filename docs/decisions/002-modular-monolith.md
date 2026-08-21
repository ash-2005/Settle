# 002 — Modular monolith

**Decision:** One Spring Boot app, packages by domain. One Next.js app. One Postgres. Docker Compose.

**Why:** A 3rd-year project should be operable by one person. Microservices add ops, not correctness.

**Not:** Kafka, Kubernetes, Redis-until-needed.
