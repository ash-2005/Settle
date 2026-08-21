# How to run Settle and how to share it

## On this Windows PC (you)

You need **Docker Desktop** running.

In the project folder (`ai repo`):

```powershell
docker compose up --build
```

Then:

- App: http://localhost:3000
- API health: http://localhost:8080/api/health

Sign in with your phone number. OTP in local Docker is always **123456**.

Stop:

```powershell
docker compose down
```

Database data is kept in a Docker volume. Wipe it with `docker compose down -v` if you want a clean slate.

If compose files are not in the repo yet (GitHub still on early slices), this machine may still have them locally — run the same command from the project folder.

### Without Docker for the website only

If Postgres + API are already up:

```powershell
cd frontend
npm install
npm run dev
```

Website: http://localhost:3000  
API must be at http://localhost:8080

---

## Sharing with friends

`localhost` is only your laptop. Friends on their phones will not see it unless you do one of these.

### 1. Friend also has Docker (best for a “real project”)

Send them the GitHub link: https://github.com/ash-2005/Settle

They install Docker, clone, `docker compose up --build`.  
This only works once the app slices are on GitHub, not when GitHub is still README-only.

Until then, they cannot “just clone.” Use option 2 or 3, or sit together on your PC.

### 2. Same Wi‑Fi (hostel / home)

1. You run `docker compose up`.
2. Find your LAN IP (PowerShell): `ipconfig` → IPv4, e.g. `192.168.1.12`
3. Friend opens `http://192.168.1.12:3000`
4. The website must call the API on your PC too. Set (and rebuild frontend)  
   `NEXT_PUBLIC_API_URL=http://192.168.1.12:8080`  
   Windows firewall may block ports 3000/8080 — allow them once.

This breaks when you leave the network or sleep the laptop.

### 3. Tunnel (friend is not on your Wi‑Fi)

Keep Docker running, then expose **two** ports (web + API), or put a reverse proxy in front later.

Quick path while developing:

- [Cloudflare Tunnel](https://developers.cloudflare.com/cloudflare-one/connections/connect-apps/) or [ngrok](https://ngrok.com/) on `3000` and `8080`
- Point the frontend env at the **public API URL**, rebuild, restart
- Send friends the public website URL

You are putting a student app on the internet. Local OTP `123456` is fine for a demo among friends; do not use this as a real bank app. No HTTPS-from-localhost claims beyond what the tunnel provides.

### 4. Later: a real host

When V1 is stable: one VPS or Railway/Render-style box, Docker Compose, HTTPS, a real OTP/SMS provider. Not required to test this week.

---

## What you can demo today (when the local V1 is running)

1. Two browsers (or phone + laptop): two phone numbers, OTP `123456`
2. Create group “Flatmates”, add the other person by phone
3. Add Dinner ₹1200, paid by you, equal split
4. Other person: home screen should show they owe ₹600
5. Settle Up: one recommended transfer
6. Add a bill with two payers if that slice/UI is present

You should be able to explain every rupee from the expense, not from a mystery total.

---

## If something does not start

- Docker Desktop is running (Linux engine)
- Port 3000 / 8080 / 5432 not taken
- `docker compose logs api` — Spring failed to reach Postgres
- Frontend shows network errors — `NEXT_PUBLIC_API_URL` does not match how you opened the site (localhost vs LAN IP vs tunnel)
