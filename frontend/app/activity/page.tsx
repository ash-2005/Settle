"use client";

import { api, inr } from "@/lib/api";
import { Shell } from "@/components/Shell";
import { useEffect, useState } from "react";

type Item = {
  id: string;
  action: string;
  payload: string;
  createdAt: string;
  actor?: { displayName: string };
};

export default function ActivityPage() {
  const [items, setItems] = useState<Item[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    api("/api/activity")
      .then((d) => setItems(d.activity || []))
      .catch((e) => setError(e.message));
  }, []);

  return (
    <Shell>
      <h1 className="text-2xl font-semibold">Activity</h1>
      {error && <p className="mt-2 text-sm text-red-700">{error}</p>}
      <div className="mt-4 space-y-2">
        {items.length === 0 && <p className="text-sm text-zinc-500">Nothing yet. Add an expense.</p>}
        {items.map((a) => {
          let payload: { description?: string; amount?: string } = {};
          try {
            payload = JSON.parse(a.payload || "{}");
          } catch {
            payload = {};
          }
          return (
            <div key={a.id} className="rounded-2xl bg-white px-4 py-3">
              <p className="font-medium">
                {a.actor?.displayName || "Someone"} {a.action.replaceAll("_", " ").toLowerCase()}
              </p>
              <p className="text-sm text-zinc-500">
                {payload.description} {payload.amount ? inr(payload.amount) : ""}
              </p>
            </div>
          );
        })}
      </div>
    </Shell>
  );
}
