"use client";

import { api, inr } from "@/lib/api";
import { Shell } from "@/components/Shell";
import Link from "next/link";
import { useEffect, useState } from "react";

type Item = {
  id: string;
  action: string;
  entityType: string;
  entityId: string;
  payload: string;
  createdAt: string;
  actor?: { displayName: string };
};

type Payload = {
  description?: string;
  amount?: string;
  oldAmount?: string;
  shares?: Record<string, { from: string; to: string }>;
};

function describe(a: Item, p: Payload, me: string) {
  const who = a.actor?.displayName || "Someone";
  const what = p.description || "an expense";
  switch (a.action) {
    case "EXPENSE_ADDED":
      return { title: `${who} added ${what}`, detail: p.amount ? inr(p.amount) : "" };
    case "EXPENSE_DELETED":
      return { title: `${who} deleted ${what}`, detail: p.amount ? inr(p.amount) : "" };
    case "EXPENSE_EDITED": {
      const mine = p.shares?.[me];
      const total = p.oldAmount && p.amount && p.oldAmount !== p.amount ? `${inr(p.oldAmount)} → ${inr(p.amount)}` : p.amount ? inr(p.amount) : "";
      const share = mine && mine.from !== mine.to ? ` Your share: ${inr(mine.from)} → ${inr(mine.to)}` : "";
      return { title: `${who} edited ${what}`, detail: `${total}.${share}` };
    }
    case "PAYMENT_MARKED":
      return { title: `${who} says they paid you`, detail: p.amount ? `${inr(p.amount)}. Confirm it in the group's Settle tab.` : "" };
    case "PAYMENT_CONFIRMED":
      return { title: `${who} confirmed your payment`, detail: p.amount ? inr(p.amount) : "" };
    case "PAYMENT_REJECTED":
      return { title: `${who} says they did not receive your payment`, detail: p.amount ? inr(p.amount) : "" };
    default:
      return { title: `${who} ${a.action.replaceAll("_", " ").toLowerCase()}`, detail: "" };
  }
}

export default function ActivityPage() {
  const [items, setItems] = useState<Item[]>([]);
  const [me, setMe] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    api("/api/me").then((u) => setMe(u.personId)).catch(() => {});
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
          let payload: Payload = {};
          try {
            payload = JSON.parse(a.payload || "{}");
          } catch {
            payload = {};
          }
          const d = describe(a, payload, me);
          const body = (
            <>
              <p className="font-medium">{d.title}</p>
              {d.detail && <p className="text-sm text-zinc-500">{d.detail}</p>}
              <p className="mt-1 text-xs text-zinc-400">{new Date(a.createdAt).toLocaleString()}</p>
            </>
          );
          return a.entityType === "EXPENSE" && a.action !== "EXPENSE_DELETED" ? (
            <Link key={a.id} href={`/expenses/${a.entityId}`} className="block rounded-2xl bg-white px-4 py-3">
              {body}
            </Link>
          ) : (
            <div key={a.id} className="rounded-2xl bg-white px-4 py-3">
              {body}
            </div>
          );
        })}
      </div>
    </Shell>
  );
}
