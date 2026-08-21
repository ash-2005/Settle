"use client";

import { api, clearTokens, getToken, inr } from "@/lib/api";
import { Shell } from "@/components/Shell";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

type Home = {
  youOwe: string;
  youAreOwed: string;
  groups: { id: string; name: string; yourNet: string }[];
  activity: { id: string; action: string; payload: string; actor?: { displayName: string } }[];
};

export default function HomePage() {
  const router = useRouter();
  const [data, setData] = useState<Home | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!getToken()) {
      router.replace("/login");
      return;
    }
    api("/api/home")
      .then(setData)
      .catch((e) => {
        if (String(e.message).includes("401") || String(e.message).toLowerCase().includes("sign in")) {
          clearTokens();
          router.replace("/login");
        } else setError(e.message);
      });
  }, [router]);

  if (!data) {
    return (
      <Shell>
        <p className="text-sm text-zinc-500">{error || "Loading…"}</p>
      </Shell>
    );
  }

  return (
    <Shell>
      <p className="text-xs font-medium tracking-wide text-zinc-500">SETTLE</p>
      <div className="mt-4 grid grid-cols-2 gap-3">
        <div className="rounded-2xl bg-white p-4">
          <p className="text-xs text-zinc-500">You owe</p>
          <p className="mt-1 text-2xl font-semibold">{inr(data.youOwe)}</p>
        </div>
        <div className="rounded-2xl bg-white p-4">
          <p className="text-xs text-zinc-500">You are owed</p>
          <p className="mt-1 text-2xl font-semibold">{inr(data.youAreOwed)}</p>
        </div>
      </div>

      <h2 className="mt-8 text-sm font-semibold text-zinc-500">Your groups</h2>
      <div className="mt-2 space-y-2">
        {data.groups.length === 0 && <p className="text-sm text-zinc-500">No groups yet. Create one from Groups.</p>}
        {data.groups.map((g) => (
          <Link key={g.id} href={`/groups/${g.id}`} className="flex items-center justify-between rounded-2xl bg-white px-4 py-3">
            <span className="font-medium">{g.name}</span>
            <span className={g.yourNet.startsWith("-") ? "text-red-700" : "text-zinc-800"}>{inr(g.yourNet)}</span>
          </Link>
        ))}
      </div>

      <h2 className="mt-8 text-sm font-semibold text-zinc-500">Recent activity</h2>
      <div className="mt-2 space-y-2">
        {data.activity.slice(0, 8).map((a) => {
          let payload: { description?: string; amount?: string } = {};
          try {
            payload = JSON.parse(a.payload || "{}");
          } catch {
            payload = {};
          }
          return (
            <div key={a.id} className="rounded-2xl bg-white px-4 py-3 text-sm">
              <p>
                {a.actor?.displayName || "Someone"} {a.action.replaceAll("_", " ").toLowerCase()}
              </p>
              {payload.description && (
                <p className="text-zinc-500">
                  {payload.description} {payload.amount ? inr(payload.amount) : ""}
                </p>
              )}
            </div>
          );
        })}
      </div>
    </Shell>
  );
}
