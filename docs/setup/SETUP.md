# Setup

## Requirements

- Docker Desktop
- Optional: Node 22+ for `frontend` without Docker; Java 21+ / Maven for `backend` without Docker

## First run

```powershell
cd "C:\Users\Ashmit\Desktop\ai repo"
docker compose up --build
```

Open http://localhost:3000  
OTP: `123456`

## Environment

Copy `.env.example` if you run the API on the host instead of Compose. Compose already injects values.

Never commit `.env`.

## Sharing with friends

`localhost` is only this PC. See [../LAUNCH.md](../LAUNCH.md).
