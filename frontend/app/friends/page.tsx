"use client";

import { api, inr } from "@/lib/api";
import { Shell } from "@/components/Shell";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

type Person = { id: string; displayName: string; pending: boolean };
type Friend = { person: Person; net: string };
type Payment = { id: string; from: Person; to: Person; amount: string; status: string };

function balanceText(net: string) {
  const n = parseFloat(net);
  if (n > 0) return { text: `owes you ${inr(net)}`, tone: "text-emerald-700" };
  if (n < 0) return { text: `you owe ${inr(net.replace("-", ""))}`, tone: "text-red-700" };
  return { text: "settled up", tone: "text-zinc-500" };
}

export default function FriendsPage() {
  const router = useRouter();
  const [friends, setFriends] = useState<Friend[]>([]);
  const [incoming, setIncoming] = useState<Payment[]>([]);
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [error, setError] = useState("");

  async function load() {
    setFriends(await api("/api/friends"));
    setIncoming(await api("/api/payments/incoming"));
  }

  useEffect(() => {
    load().catch((e) => setError(e.message));
  }, []);

  async function resolve(id: string, action: "confirm" | "reject") {
    setError("");
    try {
      await api(`/api/payments/${id}/${action}`, { method: "POST" });
      await load();
    } catch (e) {
      setError((e as Error).message);
    }
  }

  async function addFriend(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    try {
      const p = await api("/api/people/phone", { method: "POST", body: JSON.stringify({ phone, name }) });
      router.push(`/expenses/new?with=${p.id}`);
    } catch (err) {
      setError((err as Error).message);
    }
  }

  return (
    <Shell>
      <h1 className="text-2xl font-semibold">Friends</h1>
      {error && <p className="mt-2 text-sm text-red-700">{error}</p>}

      {incoming.length > 0 && (
        <div className="mt-4 space-y-2">
          <h2 className="text-sm font-semibold text-zinc-500">Waiting for you to confirm</h2>
          {incoming.map((p) => (
            <div key={p.id} className="rounded-2xl bg-white px-4 py-3">
              <p className="font-medium">
                {p.from.displayName} says they paid you {inr(p.amount)}
              </p>
              <div className="mt-2 flex gap-2">
                <button className="rounded-xl bg-zinc-900 px-3 py-1.5 text-sm text-white" onClick={() => resolve(p.id, "confirm")}>
                  Got it
                </button>
                <button className="rounded-xl bg-zinc-200 px-3 py-1.5 text-sm" onClick={() => resolve(p.id, "reject")}>
                  Not received
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      <div className="mt-4 space-y-2">
        {friends.length === 0 && <p className="text-sm text-zinc-500">No friends yet. Add an expense with someone, or add them below.</p>}
        {friends.map((f) => {
          const b = balanceText(f.net);
          return (
            <Link key={f.person.id} href={`/friends/${f.person.id}`} className="flex items-center justify-between rounded-2xl bg-white px-4 py-3">
              <span className="font-medium">
                {f.person.displayName}
                {f.person.pending && <span className="ml-2 text-xs text-zinc-400">not on Settle yet</span>}
              </span>
              <span className={`text-sm ${b.tone}`}>{b.text}</span>
            </Link>
          );
        })}
      </div>

      <form onSubmit={addFriend} className="mt-8 space-y-2 rounded-2xl bg-white p-4">
        <p className="text-sm font-medium">Add a friend by phone</p>
        <input className="w-full rounded-xl border px-3 py-2" placeholder="Name" value={name} onChange={(e) => setName(e.target.value)} required />
        <input className="w-full rounded-xl border px-3 py-2" placeholder="Phone" value={phone} onChange={(e) => setPhone(e.target.value)} required />
        <button className="w-full rounded-xl bg-zinc-100 py-2 text-sm">Add and split an expense</button>
      </form>
    </Shell>
  );
}
