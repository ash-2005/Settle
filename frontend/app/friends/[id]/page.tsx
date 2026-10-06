"use client";

import { api, inr } from "@/lib/api";
import { Shell } from "@/components/Shell";
import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";

type Person = { id: string; displayName: string };
type Payment = { id: string; from: Person; to: Person; amount: string; status: string };
type Friend = { person: Person; net: string; pendingPayments: Payment[] };
type Expense = { id: string; description: string; amount: string };

export default function FriendPage() {
  const { id } = useParams<{ id: string }>();
  const [f, setF] = useState<Friend | null>(null);
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [me, setMe] = useState("");
  const [amount, setAmount] = useState("");
  const [error, setError] = useState("");

  async function load() {
    const [friend, list, u] = await Promise.all([api(`/api/friends/${id}`), api(`/api/people/${id}/expenses`), api("/api/me")]);
    setF(friend);
    setExpenses(list);
    setMe(u.personId);
    const n = parseFloat(friend.net);
    setAmount(n !== 0 ? Math.abs(n).toFixed(2) : "");
  }

  useEffect(() => {
    load().catch((e) => setError(e.message));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  async function act(path: string, body?: object) {
    setError("");
    try {
      await api(path, { method: "POST", body: body ? JSON.stringify(body) : undefined });
      await load();
    } catch (e) {
      setError((e as Error).message);
    }
  }

  if (!f) return <Shell><p className="text-sm text-zinc-500">{error || "Loading…"}</p></Shell>;

  const n = parseFloat(f.net);
  return (
    <Shell>
      <h1 className="text-2xl font-semibold">{f.person.displayName}</h1>
      <p className={`mt-1 text-lg ${n > 0 ? "text-emerald-700" : n < 0 ? "text-red-700" : "text-zinc-500"}`}>
        {n > 0 ? `owes you ${inr(f.net)}` : n < 0 ? `you owe ${inr(f.net.replace("-", ""))}` : "settled up"}
      </p>
      <p className="mt-1 text-xs text-zinc-500">Across every group and one-off expense you share.</p>
      {error && <p className="mt-2 text-sm text-red-700">{error}</p>}

      <Link href={`/expenses/new?with=${f.person.id}`} className="mt-4 block rounded-2xl bg-zinc-900 py-3 text-center text-white">
        Add expense with {f.person.displayName}
      </Link>

      {n < 0 && (
        <div className="mt-4 space-y-2 rounded-2xl bg-white p-4">
          <p className="text-sm font-medium">Record a payment to {f.person.displayName}</p>
          <input className="w-full rounded-xl border px-3 py-2" inputMode="decimal" value={amount} onChange={(e) => setAmount(e.target.value)} />
          <button className="w-full rounded-xl bg-zinc-900 py-2 text-sm text-white" onClick={() => act("/api/payments", { toPersonId: f.person.id, amount })}>
            I paid this
          </button>
          <p className="text-xs text-zinc-500">{f.person.displayName} has to confirm before your balance changes. Settle does not move money.</p>
        </div>
      )}

      {f.pendingPayments.map((p) => (
        <div key={p.id} className="mt-3 rounded-2xl bg-white px-4 py-3">
          <p className="font-medium">
            {p.from.displayName} paid {p.to.displayName} {inr(p.amount)}
          </p>
          <p className="text-sm text-zinc-500">Waiting for {p.to.displayName} to confirm</p>
          {p.to.id === me && (
            <div className="mt-2 flex gap-2">
              <button className="rounded-xl bg-zinc-900 px-3 py-1.5 text-sm text-white" onClick={() => act(`/api/payments/${p.id}/confirm`)}>
                Got it
              </button>
              <button className="rounded-xl bg-zinc-200 px-3 py-1.5 text-sm" onClick={() => act(`/api/payments/${p.id}/reject`)}>
                Not received
              </button>
            </div>
          )}
        </div>
      ))}

      <h2 className="mt-6 text-sm font-semibold text-zinc-500">One-off expenses together</h2>
      <div className="mt-2 space-y-2">
        {expenses.length === 0 && <p className="text-sm text-zinc-500">None. Group expenses are in each group.</p>}
        {expenses.map((x) => (
          <Link key={x.id} href={`/expenses/${x.id}`} className="flex justify-between rounded-2xl bg-white px-4 py-3">
            <span>{x.description}</span>
            <span>{inr(x.amount)}</span>
          </Link>
        ))}
      </div>
    </Shell>
  );
}
