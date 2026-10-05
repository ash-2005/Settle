# Deploy Settle (website first, then Android app)

No Docker on your side. The host builds everything from GitHub.

## 1. Website on Render (free tier)

1. Push this repo to GitHub (done) and sign in at https://render.com with GitHub.
2. **New > Blueprint**, pick `ash-2005/Settle`. Render reads `render.yaml` and creates the database, the API (`settle-api`) and the website (`settle-web`).
3. It asks for two values:
   - `SETTLE_OTP`: the login code your friends will type. Pick your own, not `123456`.
   - `API_INTERNAL_URL`: leave blank for now.
4. When `settle-api` is live, copy its URL (looks like `https://settle-api-xxxx.onrender.com`), open `settle-web` > Environment, set `API_INTERNAL_URL` to it and redeploy the web service. The site bakes this in at build, so it needs that redeploy.
5. Send friends the `settle-web` URL.

Check: `<settle-api url>/api/health` returns `{"status":"ok"}`.

Free tier notes: the services sleep after ~15 minutes idle (first load takes about a minute), and the free Postgres is deleted after 30 days. Upgrade the database before you keep real data.

**Login warning.** There is no SMS provider yet. The code in `SETTLE_OTP` works for any phone number, so only share the link with people you trust. Add an SMS provider (MSG91/Twilio) before a public launch.

## 2. Android app (after the website works)

Needs Android Studio (not available in the cloud session).

1. In `frontend/`: `npm i @capacitor/core @capacitor/cli @capacitor/android`
2. `npx cap init Settle app.settle.android --web-dir=public`
3. In `capacitor.config.ts` set `server: { url: "https://<settle-web url>" }` so the app loads the live site.
4. `npx cap add android && npx cap open android`, then Build > Generate Signed APK/AAB.

Until then: open the site in Chrome > menu > Add to Home screen (the PWA).

## Alternative: your own server

`docker-compose.prod.yml` runs everything behind Caddy HTTPS on any VPS. See the comments at the top of that file.
