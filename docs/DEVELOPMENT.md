# Development

Windows. Docker Desktop required for the full stack. Java 22 is installed; Docker builds with **21**. Node 26+ is installed. **Maven is not** on PATH — use `backend/mvnw` or the API Docker image.

## Day-to-day

See [LAUNCH.md](LAUNCH.md) and [CONTEXT.md](CONTEXT.md).

```powershell
docker compose up --build
```

Backend tests inside the image or:

```powershell
cd backend
.\mvnw.cmd test
```

Frontend:

```powershell
cd frontend
npm test
npm run build
```

## Git

- Author: Ashmit `<ashmitg25@gmail.com>`
- Strip Cursor co-author (CONTEXT.md)
- One slice from [COMMIT_PLAN.md](COMMIT_PLAN.md) / [SLICE_FILES.md](SLICE_FILES.md)
- Never `git add .`
- Never `--force` on `main`
- Never change git config from the agent

## Env

Copy `.env.example` to `.env` if present. Do not commit `.env`.

## Code style

Small methods, obvious names, comments for **why** (rounding, settlement honesty, audience). No extra frameworks for dopamine.
