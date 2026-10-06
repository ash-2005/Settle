"use client";

import { api, inr } from "@/lib/api";
import { Shell } from "@/components/Shell";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";

type Person = { id: string; displayName: string };
type Expense = {
  id: string;
  description: string;
  amount: string;
  status: string;
  splitMethod: string;
  payers: { person: Person; amount: string }[];
  participants: { person: Person; shareAmount: string }[];
};

export default function ExpenseDetailPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const [e, setE] = useState<Expense | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    api(`/api/expenses/${id}`).then(setE).catch((err) => setError(err.message));
  }, [id]);

  async function del() {
    if (!confirm("Delete this expense? Balances update; the history is kept.")) return;
    try {
      await api(`/api/expenses/${id}/delete`, { method: "POST" });
      router.back();
    } catch (err) {
      setError((err as Error).message);
    }
  }

  if (!e) {
    return (
      <Shell>
        <p className="text-sm text-zinc-500">{error || "Loading…"}</p>
      </Shell>
    );
  }

  return (
    <Shell>
      <h1 className="text-2xl font-semibold">{e.description}</h1>
      <p className="mt-1 text-3xl font-semibold">{inr(e.amount)}</p>
      <p className="mt-1 text-sm text-zinc-500">
        {e.splitMethod.toLowerCase()} split · {e.status.toLowerCase()}
      </p>
      <h2 className="mt-6 text-sm font-semibold text-zinc-500">Paid by</h2>
      {e.payers.map((p) => (
        <div key={p.person.id} className="flex justify-between rounded-2xl bg-white px-4 py-3 text-sm">
          <span>{p.person.displayName}</span>
          <span>{inr(p.amount)}</span>
        </div>
      ))}
      <h2 className="mt-6 text-sm font-semibold text-zinc-500">Shares</h2>
      {e.participants.map((p) => (
        <div key={p.person.id} className="mt-2 flex justify-between rounded-2xl bg-white px-4 py-3 text-sm">
          <span>{p.person.displayName}</span>
          <span>{inr(p.shareAmount)}</span>
        </div>
      ))}
      {e.status === "ACTIVE" && (
        <div className="mt-6 flex gap-2">
          <Link href={`/expenses/new?edit=${e.id}`} className="flex-1 rounded-xl bg-zinc-900 py-2 text-center text-sm text-white">
            Edit
          </Link>
          <button onClick={del} className="flex-1 rounded-xl border border-zinc-300 py-2 text-sm text-zinc-600">
            Delete
          </button>
        </div>
      )}
      {error && <p className="mt-2 text-sm text-red-700">{error}</p>}
    </Shell>
  );
}
