"use client";

import { api, inr } from "@/lib/api";
import { Shell } from "@/components/Shell";
import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";

type Person = { id: string; displayName: string; pending: boolean; phone: string };
type Member = { person: Person; role: string; status: string };
type Group = { id: string; name: string; members: Member[] };
type Expense = { id: string; description: string; amount: string };
type Balances = { yourNet: string; members: { person: Person; net: string }[]; why: { expenseId: string; description: string; delta: string }[] };
type Plan = { note: string; transfers: { from: Person; to: Person; amount: string }[] };

export default function GroupDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [tab, setTab] = useState<"overview" | "expenses" | "balances" | "settle">("overview");
  const [group, setGroup] = useState<Group | null>(null);
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [balances, setBalances] = useState<Balances | null>(null);
  const [plan, setPlan] = useState<Plan | null>(null);
  const [phone, setPhone] = useState("");
  const [personName, setPersonName] = useState("");
  const [error, setError] = useState("");

  async function load() {
    const g = await api(`/api/groups/${id}`);
    setGroup(g);
    setExpenses(await api(`/api/groups/${id}/expenses`));
    setBalances(await api(`/api/groups/${id}/balances`));
    setPlan(await api(`/api/groups/${id}/settlement-plan`));
  }

  useEffect(() => {
    load().catch((e) => setError(e.message));
  }, [id]);

  async function addMember(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    try {
      const person = await api("/api/people/phone", { method: "POST", body: JSON.stringify({ phone, name: personName }) });
      await api(`/api/groups/${id}/members`, { method: "POST", body: JSON.stringify({ personId: person.id }) });
      setPhone("");
      setPersonName("");
      await load();
    } catch (err) {
      setError((err as Error).message);
    }
  }

  if (!group) {
    return (
      <Shell>
        <p className="text-sm text-zinc-500">{error || "Loading…"}</p>
      </Shell>
    );
  }

  const tabs = [
    ["overview", "Overview"],
    ["expenses", "Expenses"],
    ["balances", "Balances"],
    ["settle", "Settle"],
  ] as const;

  return (
    <Shell>
      <h1 className="text-2xl font-semibold">{group.name}</h1>
      <div className="mt-4 flex gap-1 overflow-x-auto text-sm">
        {tabs.map(([key, label]) => (
          <button
            key={key}
            onClick={() => setTab(key)}
            className={`rounded-full px-3 py-1 ${tab === key ? "bg-zinc-900 text-white" : "bg-white text-zinc-600"}`}
          >
            {label}
          </button>
        ))}
      </div>

      {error && <p className="mt-3 text-sm text-red-700">{error}</p>}

      {tab === "overview" && (
        <div className="mt-4 space-y-3">
          <p className="text-sm text-zinc-600">{group.members.filter((m) => m.status === "ACTIVE").length} members</p>
          <Link href={`/expenses/new?groupId=${group.id}`} className="block rounded-2xl bg-zinc-900 py-3 text-center text-white">
            Add expense
          </Link>
          <form onSubmit={addMember} className="space-y-2 rounded-2xl bg-white p-4">
            <p className="text-sm font-medium">Add person by phone</p>
            <input className="w-full rounded-xl border px-3 py-2" placeholder="Name" value={personName} onChange={(e) => setPersonName(e.target.value)} />
            <input className="w-full rounded-xl border px-3 py-2" placeholder="Phone" value={phone} onChange={(e) => setPhone(e.target.value)} required />
            <button className="w-full rounded-xl bg-zinc-100 py-2 text-sm">Add to group</button>
          </form>
          <ul className="space-y-1 text-sm">
            {group.members.map((m) => (
              <li key={m.person.id} className="flex justify-between rounded-xl bg-white px-3 py-2">
                <span>
                  {m.person.displayName}
                  {m.person.pending ? " · invite pending" : ""}
                </span>
                <span className="text-zinc-500">{m.status}</span>
              </li>
            ))}
          </ul>
        </div>
      )}

      {tab === "expenses" && (
        <div className="mt-4 space-y-2">
          {expenses.length === 0 && <p className="text-sm text-zinc-500">No expenses yet.</p>}
          {expenses.map((e) => (
            <Link key={e.id} href={`/expenses/${e.id}`} className="flex justify-between rounded-2xl bg-white px-4 py-3">
              <span>{e.description}</span>
              <span>{inr(e.amount)}</span>
            </Link>
          ))}
        </div>
      )}

      {tab === "balances" && balances && (
        <div className="mt-4 space-y-3">
          <p className="text-lg font-semibold">Your net {inr(balances.yourNet)}</p>
          {balances.members.map((m) => (
            <div key={m.person.id} className="flex justify-between rounded-2xl bg-white px-4 py-3 text-sm">
              <span>{m.person.displayName}</span>
              <span>{inr(m.net)}</span>
            </div>
          ))}
          <h2 className="pt-2 text-sm font-semibold text-zinc-500">Why this number</h2>
          {balances.why.length === 0 && <p className="text-sm text-zinc-500">Nothing to explain — you are even here.</p>}
          {balances.why.map((w) => (
            <Link key={w.expenseId} href={`/expenses/${w.expenseId}`} className="flex justify-between rounded-2xl bg-white px-4 py-3 text-sm">
              <span>{w.description}</span>
              <span>{inr(w.delta)}</span>
            </Link>
          ))}
        </div>
      )}

      {tab === "settle" && plan && (
        <div className="mt-4 space-y-3">
          <p className="text-sm text-zinc-600">{plan.note}</p>
          {plan.transfers.length === 0 && <p className="rounded-2xl bg-white p-4 text-sm">Everyone is settled in this group.</p>}
          {plan.transfers.map((t, i) => (
            <div key={i} className="rounded-2xl bg-white px-4 py-3">
              <p className="font-medium">
                {t.from.displayName} → {t.to.displayName}
              </p>
              <p className="text-zinc-600">{inr(t.amount)}</p>
            </div>
          ))}
        </div>
      )}
    </Shell>
  );
}
