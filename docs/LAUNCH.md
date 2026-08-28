# How to run Settle and how to share it

## Launch on this Windows PC (do this)

1. Start **Docker Desktop** and wait until it says running.
2. Open PowerShell in the project folder:

```powershell
cd "C:\Users\Ashmit\Desktop\ai repo"
docker compose up --build
```

3. Browser: **http://localhost:3000**
4. Phone number (10 digits). Code: **123456** (local demo — not a real SMS).
5. Create a group, add a second phone, add an expense.

The stack is three containers: Postgres, API `:8080`, website `:3000`.

Stop: `docker compose down`

---

## Phone “app” (no Play Store APK on this PC)

This machine has **no Android SDK**, so a signed `.apk` cannot be compiled here.

On your **Android phone (same Wi‑Fi)**:

1. Find the PC’s IPv4 (`ipconfig`).
2. Rebuild web with that API URL, or for a quick solo demo use the PC browser.
3. In Chrome: site menu → **Add to Home screen**. That is a PWA (looks like an app, OTP login still works).

A real Play Store APK needs Android Studio installed, then wrapping this web app (Capacitor/TWA). That is a later packaging step, not a second product.

---

## OTP

Local Docker always accepts **123456**. Logs also print the code. Real SMS (MSG91/Twilio) is a provider swap behind the same `/api/auth/otp/*` routes when you have an API key.

---

## AI

On **Add expense**: type or **Speak**, then **Fill form**. You still tap **Add expense**.

- Without `GEMINI_API_KEY`: the Java parser fills the form.
- With a Gemini key in `.env` / Compose: Gemini can suggest the draft. It still does not save until you confirm.

---

## Sharing with friends

`localhost` is only this laptop. Friends on their phones will not see it unless you do one of these.

### 1. Friend also has Docker

Send https://github.com/ash-2005/Settle — they run `docker compose up --build`.

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
