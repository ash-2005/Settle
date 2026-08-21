"use client";

import { api, setTokens } from "@/lib/api";
import { useRouter } from "next/navigation";
import { useState } from "react";

export default function LoginPage() {
  const router = useRouter();
  const [phone, setPhone] = useState("");
  const [code, setCode] = useState("123456");
  const [name, setName] = useState("");
  const [step, setStep] = useState<"phone" | "otp">("phone");
  const [error, setError] = useState("");

  async function requestOtp(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    try {
      await api("/api/auth/otp/request", { method: "POST", body: JSON.stringify({ phone }) });
      setStep("otp");
    } catch (err) {
      setError((err as Error).message);
    }
  }

  async function verify(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    try {
      const data = await api("/api/auth/otp/verify", {
        method: "POST",
        body: JSON.stringify({ phone, code, displayName: name }),
      });
      setTokens(data.accessToken, data.refreshToken);
      router.replace("/");
    } catch (err) {
      setError((err as Error).message);
    }
  }

  return (
    <div className="mx-auto flex min-h-dvh max-w-md flex-col justify-center px-6">
      <h1 className="text-3xl font-semibold">Settle</h1>
      <p className="mt-2 text-sm text-zinc-600">Shared expenses without the mystery total.</p>
      {step === "phone" ? (
        <form onSubmit={requestOtp} className="mt-8 space-y-3">
          <label className="block text-sm">
            Phone
            <input
              className="mt-1 w-full rounded-xl border border-zinc-300 bg-white px-3 py-3"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              placeholder="10-digit number"
              required
            />
          </label>
          <button className="w-full rounded-xl bg-zinc-900 py-3 text-white">Send code</button>
        </form>
      ) : (
        <form onSubmit={verify} className="mt-8 space-y-3">
          <p className="text-sm text-zinc-600">Local OTP is always 123456. Nothing is SMSed.</p>
          <label className="block text-sm">
            Your name
            <input className="mt-1 w-full rounded-xl border border-zinc-300 bg-white px-3 py-3" value={name} onChange={(e) => setName(e.target.value)} placeholder="Ashmit" />
          </label>
          <label className="block text-sm">
            Code
            <input className="mt-1 w-full rounded-xl border border-zinc-300 bg-white px-3 py-3" value={code} onChange={(e) => setCode(e.target.value)} required />
          </label>
          <button className="w-full rounded-xl bg-zinc-900 py-3 text-white">Continue</button>
        </form>
      )}
      {error && <p className="mt-4 text-sm text-red-700">{error}</p>}
    </div>
  );
}
