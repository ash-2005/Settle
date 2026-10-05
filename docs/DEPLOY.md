# Deploy Settle (website first, then Android app)

## 1. Website (one HTTPS URL)

You need a VPS (or any Docker host) and a domain with an A record pointing at it.

```bash
git clone https://github.com/ash-2005/Settle && cd Settle
cp .env.example .env     # set SETTLE_DOMAIN, POSTGRES_PASSWORD, JWT_SECRET (32+ random chars), SETTLE_OTP
docker compose -f docker-compose.prod.yml up -d --build
```

Caddy gets the HTTPS certificate automatically. The browser talks to `/api` on the same origin; Next forwards it to the Spring API, so there is no CORS or `NEXT_PUBLIC_API_URL` juggling.

**OTP warning.** There is no SMS provider yet. Login accepts the single code in `SETTLE_OTP` for any phone number, so anyone who finds the URL can sign in as any number. That is fine for a private demo among friends only. Before a real public launch, put an SMS provider (MSG91/Twilio) behind `/api/auth/otp/*`.

## 2. Android app (after the website is live)

This needs Android Studio (not available on every machine).

1. `npm i @capacitor/core @capacitor/cli @capacitor/android` in `frontend/`
2. `npx cap init Settle app.settle.android --web-dir=public`
3. In `capacitor.config.ts` set `server: { url: "https://<your domain>" }` so the app shell loads the live site.
4. `npx cap add android && npx cap open android`, then Build > Generate Signed APK/AAB.

Until then the PWA works: open the site in Chrome, menu > Add to Home screen.
