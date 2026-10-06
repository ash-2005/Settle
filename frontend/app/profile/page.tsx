"use client";

import { api, clearTokens } from "@/lib/api";
import { Shell } from "@/components/Shell";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function ProfilePage() {
  const router = useRouter();
  const [me, setMe] = useState<{ displayName: string; username: string; phone: string } | null>(null);

  const [name, setName] = useState("");
  const [msg, setMsg] = useState("");

  useEffect(() => {
    api("/api/me")
      .then((u) => {
        setMe(u);
        setName(u.displayName);
      })
      .catch(() => router.replace("/login"));
  }, [router]);

  async function save(e: React.FormEvent) {
    e.preventDefault();
    setMsg("");
    try {
      await api("/api/me", { method: "PATCH", body: JSON.stringify({ displayName: name }) });
      setMe(me && { ...me, displayName: name.trim() });
      setMsg("Saved. Friends will see this name.");
    } catch (err) {
      setMsg((err as Error).message);
    }
  }

  return (
    <Shell>
      <h1 className="text-2xl font-semibold">Profile</h1>
      {me && (
        <div className="mt-4 rounded-2xl bg-white p-4 text-sm">
          <p className="text-lg font-medium">{me.displayName}</p>
          <p className="text-zinc-500">@{me.username}</p>
          <p className="mt-2 text-zinc-500">{me.phone}</p>
        </div>
      )}
      <form onSubmit={save} className="mt-4 space-y-2">
        <label className="block text-sm">
          Your name (shown to friends)
          <input className="mt-1 w-full rounded-xl border border-zinc-300 bg-white px-3 py-2" value={name} onChange={(e) => setName(e.target.value)} maxLength={60} required />
        </label>
        <button className="w-full rounded-xl bg-zinc-900 py-2 text-white">Save name</button>
        {msg && <p className="text-sm text-zinc-600">{msg}</p>}
      </form>
      <p className="mt-4 text-sm text-zinc-500">A student project, not a bank. Settle does not move money; it only tracks who owes whom.</p>
      <button
        className="mt-6 w-full rounded-xl border border-zinc-300 py-2"
        onClick={() => {
          clearTokens();
          router.replace("/login");
        }}
      >
        Sign out
      </button>
    </Shell>
  );
}
