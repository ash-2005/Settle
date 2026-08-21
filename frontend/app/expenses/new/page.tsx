"use client";

import { api } from "@/lib/api";
import { Shell } from "@/components/Shell";
import { useRouter, useSearchParams } from "next/navigation";
import { Suspense, useEffect, useState } from "react";

type Person = { id: string; displayName: string };
type Group = { id: string; name: string; members: { person: Person; status: string }[] };

function NewExpenseForm() {
  const router = useRouter();
  const params = useSearchParams();
  const presetGroup = params.get("groupId") || "";
  const [groups, setGroups] = useState<{ id: string; name: string }[]>([]);
  const [groupId, setGroupId] = useState(presetGroup);
  const [group, setGroup] = useState<Group | null>(null);
  const [me, setMe] = useState<Person | null>(null);
  const [amount, setAmount] = useState("");
  const [description, setDescription] = useState("");
  const [selected, setSelected] = useState<string[]>([]);
    const [payerId, setPayerId] = useState("");
    const [error, setError] = useState("");
  const [phone, setPhone] = useState("");
  const [extraPeople, setExtraPeople] = useState<Person[]>([]);

  useEffect(() => {
    api("/api/me").then((u) => {
      setMe({ id: u.personId, displayName: u.displayName });
      setPayerId(u.personId);
      setSelected([u.personId]);
    });
    api("/api/groups").then(setGroups);
  }, []);

  useEffect(() => {
    if (!groupId) {
      setGroup(null);
      return;
    }
    api(`/api/groups/${groupId}`).then((g: Group) => {
      setGroup(g);
      const active = g.members.filter((m) => m.status === "ACTIVE").map((m) => m.person.id);
      setSelected(active);
    });
  }, [groupId]);

  const people: Person[] = group
    ? group.members.filter((m) => m.status === "ACTIVE").map((m) => m.person)
    : [...(me ? [me] : []), ...extraPeople];

  async function addPhone(e: React.FormEvent) {
    e.preventDefault();
    const p = await api("/api/people/phone", { method: "POST", body: JSON.stringify({ phone }) });
    setExtraPeople((prev) => [...prev, p]);
    setSelected((prev) => Array.from(new Set([...prev, p.id])));
    setPhone("");
  }

  function toggle(id: string) {
    setSelected((prev) => (prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]));
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    try {
      const created = await api("/api/expenses", {
        method: "POST",
        body: JSON.stringify({
          amount,
          description,
          groupId: groupId || null,
          participantIds: selected,
          payers: [{ personId: payerId, amount }],
          splitMethod: "EQUAL",
        }),
      });
      router.push(`/expenses/${created.id}`);
    } catch (err) {
      setError((err as Error).message);
    }
  }

  return (
    <Shell>
      <h1 className="text-2xl font-semibold">Add expense</h1>
      <form onSubmit={submit} className="mt-4 space-y-3">
        <select className="w-full rounded-xl border bg-white px-3 py-3" value={groupId} onChange={(e) => setGroupId(e.target.value)}>
          <option value="">No group — just these people</option>
          {groups.map((g) => (
            <option key={g.id} value={g.id}>
              {g.name}
            </option>
          ))}
        </select>
        <input className="w-full rounded-xl border bg-white px-3 py-3 text-2xl" placeholder="₹ amount" value={amount} onChange={(e) => setAmount(e.target.value)} required inputMode="decimal" />
        <input className="w-full rounded-xl border bg-white px-3 py-3" placeholder="What for?" value={description} onChange={(e) => setDescription(e.target.value)} required />
        <label className="block text-sm">
          Paid by
          <select className="mt-1 w-full rounded-xl border bg-white px-3 py-2" value={payerId} onChange={(e) => setPayerId(e.target.value)}>
            {people.map((p) => (
              <option key={p.id} value={p.id}>
                {p.displayName}
              </option>
            ))}
          </select>
        </label>
        <div>
          <p className="text-sm font-medium">Participants</p>
          <div className="mt-2 flex flex-wrap gap-2">
            {people.map((p) => (
              <button type="button" key={p.id} onClick={() => toggle(p.id)} className={`rounded-full px-3 py-1 text-sm ${selected.includes(p.id) ? "bg-zinc-900 text-white" : "bg-white"}`}>
                {p.displayName}
              </button>
            ))}
          </div>
        </div>
        {!groupId && (
          <div className="flex gap-2">
            <input className="flex-1 rounded-xl border bg-white px-3 py-2 text-sm" placeholder="Add by phone" value={phone} onChange={(e) => setPhone(e.target.value)} />
            <button type="button" onClick={addPhone} className="rounded-xl bg-white px-3 text-sm">
              Add
            </button>
          </div>
        )}
        <p className="text-sm text-zinc-500">Split equally. Exact / percent / shares are on the API for tests and later UI.</p>
        <button className="w-full rounded-xl bg-zinc-900 py-3 text-white">Add expense</button>
      </form>
      {error && <p className="mt-3 text-sm text-red-700">{error}</p>}
    </Shell>
  );
}

export default function NewExpensePage() {
  return (
    <Suspense>
      <NewExpenseForm />
    </Suspense>
  );
}
