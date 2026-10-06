"use client";

import { api } from "@/lib/api";
import { Shell } from "@/components/Shell";
import { useRouter, useSearchParams } from "next/navigation";
import { Suspense, useEffect, useState } from "react";

type Person = { id: string; displayName: string };
type Method = "EQUAL" | "EXACT" | "PERCENTAGE" | "SHARES";
type ExpenseView = {
  groupId: string | null;
  description: string;
  amount: string;
  splitMethod: string;
  payers: { person: Person; amount: string }[];
  participants: { person: Person; shareAmount: string }[];
};
type Group = { id: string; name: string; members: { person: Person; status: string }[] };

function NewExpenseForm() {
  const router = useRouter();
  const params = useSearchParams();
  const presetGroup = params.get("groupId") || "";
  const editId = params.get("edit") || "";
  const withId = params.get("with") || "";
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
  const [aiText, setAiText] = useState("");
  const [aiNote, setAiNote] = useState("");
  const [listening, setListening] = useState(false);
  const [splitMethod, setSplitMethod] = useState<Method>("EQUAL");
  const [values, setValues] = useState<Record<string, string>>({});
  const [multiPayer, setMultiPayer] = useState(false);
  const [payerAmounts, setPayerAmounts] = useState<Record<string, string>>({});

  useEffect(() => {
    api("/api/me").then((u) => {
      setMe({ id: u.personId, displayName: u.displayName });
      setPayerId(u.personId);
      setSelected([u.personId]);
    });
    api("/api/groups").then(setGroups);
  }, []);

  useEffect(() => {
    if (!withId || editId) return;
    api(`/api/friends/${withId}`).then((f: { person: Person }) => {
      setExtraPeople([f.person]);
      setSelected((prev) => Array.from(new Set([...prev, f.person.id])));
    });
  }, [withId, editId]);

  useEffect(() => {
    if (!editId) return;
    api(`/api/expenses/${editId}`).then((x: ExpenseView) => {
      setGroupId(x.groupId || "");
      setAmount(x.amount);
      setDescription(x.description);
      setSelected(x.participants.map((p) => p.person.id));
      setExtraPeople(x.participants.map((p) => p.person));
      if (x.splitMethod === "EQUAL") {
        setSplitMethod("EQUAL");
      } else {
        // Percent and shares were turned into exact amounts when saved; edit them as exact.
        setSplitMethod("EXACT");
        setValues(Object.fromEntries(x.participants.map((p) => [p.person.id, p.shareAmount])));
      }
      if (x.payers.length > 1) {
        setMultiPayer(true);
        setPayerAmounts(Object.fromEntries(x.payers.map((p) => [p.person.id, p.amount])));
      } else if (x.payers[0]) {
        setPayerId(x.payers[0].person.id);
      }
    });
  }, [editId]);

  useEffect(() => {
    if (!groupId) {
      setGroup(null);
      return;
    }
    api(`/api/groups/${groupId}`).then((g: Group) => {
      setGroup(g);
      if (!editId) {
        setSelected(g.members.filter((m) => m.status === "ACTIVE").map((m) => m.person.id));
      }
    });
  }, [groupId, editId]);

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

  async function fillFromAi() {
    setError("");
    try {
      const draft = await api("/api/ai/expense-draft", {
        method: "POST",
        body: JSON.stringify({ text: aiText, groupId: groupId || null }),
      });
      if (draft.amount) setAmount(draft.amount);
      if (draft.description) setDescription(draft.description);
      if (draft.payerId) setPayerId(draft.payerId);
      if (Array.isArray(draft.participantIds) && draft.participantIds.length) setSelected(draft.participantIds);
      setAiNote(draft.note || "Review the fields, then tap Add expense.");
    } catch (err) {
      setError((err as Error).message);
    }
  }

  function listen() {
    const Speech = (window as unknown as { webkitSpeechRecognition?: new () => SpeechRec }).webkitSpeechRecognition
      || (window as unknown as { SpeechRecognition?: new () => SpeechRec }).SpeechRecognition;
    if (!Speech) {
      setError("Voice works in Chrome / Edge on this phone or PC.");
      return;
    }
    const rec = new Speech();
    rec.lang = "en-IN";
    rec.onresult = (ev: { results: { 0: { 0: { transcript: string } } } }) => {
      setAiText(ev.results[0][0].transcript);
      setListening(false);
    };
    rec.onerror = () => setListening(false);
    rec.onend = () => setListening(false);
    setListening(true);
    rec.start();
  }

  type SpeechRec = {
    lang: string;
    start: () => void;
    onresult: ((ev: { results: { 0: { 0: { transcript: string } } } }) => void) | null;
    onerror: (() => void) | null;
    onend: (() => void) | null;
  };

  function sumOf(ids: string[], vals: Record<string, string>) {
    // Whole paise, so the check never depends on float rounding.
    return ids.reduce((t, id) => t + Math.round(parseFloat(vals[id] || "0") * 100), 0);
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    const body: Record<string, unknown> = {
      amount,
      description,
      participantIds: selected,
      splitMethod,
    };
    if (!editId) body.groupId = groupId || null;
    const total = Math.round(parseFloat(amount || "0") * 100);
    if (multiPayer) {
      const ids = people.map((p) => p.id).filter((id) => parseFloat(payerAmounts[id] || "0") > 0);
      if (sumOf(ids, payerAmounts) !== total) {
        setError("Amounts paid must add up to the expense total.");
        return;
      }
      body.payers = ids.map((id) => ({ personId: id, amount: payerAmounts[id] }));
    } else {
      body.payers = [{ personId: payerId, amount }];
    }
    if (splitMethod === "EXACT") {
      if (sumOf(selected, values) !== total) {
        setError("Shares must add up to the expense total.");
        return;
      }
      body.exactAmounts = Object.fromEntries(selected.map((id) => [id, values[id] || "0"]));
    } else if (splitMethod === "PERCENTAGE") {
      if (sumOf(selected, values) !== 10000) {
        setError("Percentages must add up to 100.");
        return;
      }
      body.percentages = Object.fromEntries(selected.map((id) => [id, values[id] || "0"]));
    } else if (splitMethod === "SHARES") {
      body.shareCounts = Object.fromEntries(selected.map((id) => [id, parseInt(values[id] || "1", 10) || 1]));
    }
    try {
      const saved = await api(editId ? `/api/expenses/${editId}` : "/api/expenses", {
        method: editId ? "PATCH" : "POST",
        body: JSON.stringify(body),
      });
      router.push(`/expenses/${saved.id}`);
    } catch (err) {
      setError((err as Error).message);
    }
  }

  return (
    <Shell>
      <h1 className="text-2xl font-semibold">{editId ? "Edit expense" : "Add expense"}</h1>
      {!editId && (
      <div className="mt-4 space-y-2 rounded-2xl bg-white p-4">
        <p className="text-sm font-medium">Type or speak — then review</p>
        <textarea className="w-full rounded-xl border px-3 py-2 text-sm" rows={3} placeholder='I paid 1850 for groceries split between me and Rahul' value={aiText} onChange={(e) => setAiText(e.target.value)} />
        <div className="flex gap-2">
          <button type="button" onClick={listen} className="flex-1 rounded-xl bg-zinc-100 py-2 text-sm">
            {listening ? "Listening…" : "Speak"}
          </button>
          <button type="button" onClick={fillFromAi} className="flex-1 rounded-xl bg-zinc-900 py-2 text-sm text-white">
            Fill form
          </button>
        </div>
        {aiNote && <p className="text-xs text-zinc-500">{aiNote}</p>}
      </div>
      )}
      <form onSubmit={submit} className="mt-4 space-y-3">
        <select disabled={!!editId} className="w-full rounded-xl border bg-white px-3 py-3 disabled:opacity-60" value={groupId} onChange={(e) => setGroupId(e.target.value)}>
          <option value="">No group — just these people</option>
          {groups.map((g) => (
            <option key={g.id} value={g.id}>
              {g.name}
            </option>
          ))}
        </select>
        <input className="w-full rounded-xl border bg-white px-3 py-3 text-2xl" placeholder="₹ amount" value={amount} onChange={(e) => setAmount(e.target.value)} required inputMode="decimal" />
        <input className="w-full rounded-xl border bg-white px-3 py-3" placeholder="What for?" value={description} onChange={(e) => setDescription(e.target.value)} required />
        <div className="text-sm">
          <div className="flex items-center justify-between">
            <span>Paid by</span>
            <button type="button" className="text-zinc-500 underline" onClick={() => setMultiPayer(!multiPayer)}>
              {multiPayer ? "One person paid" : "Multiple people paid"}
            </button>
          </div>
          {!multiPayer && (
            <select className="mt-1 w-full rounded-xl border bg-white px-3 py-2" value={payerId} onChange={(e) => setPayerId(e.target.value)}>
              {people.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.displayName}
                </option>
              ))}
            </select>
          )}
          {multiPayer && (
            <div className="mt-2 space-y-2">
              {people.map((p) => (
                <div key={p.id} className="flex items-center gap-2">
                  <span className="flex-1">{p.displayName}</span>
                  <input className="w-28 rounded-xl border bg-white px-3 py-2 text-right" inputMode="decimal" placeholder="0" value={payerAmounts[p.id] || ""} onChange={(e) => setPayerAmounts({ ...payerAmounts, [p.id]: e.target.value })} />
                </div>
              ))}
            </div>
          )}
        </div>
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
        <div>
          <p className="text-sm font-medium">Split</p>
          <div className="mt-2 grid grid-cols-4 gap-1 rounded-xl bg-zinc-200 p-1 text-sm">
            {([["EQUAL", "Equally"], ["EXACT", "₹ Exact"], ["PERCENTAGE", "%"], ["SHARES", "Shares"]] as [Method, string][]).map(([m, label]) => (
              <button type="button" key={m} onClick={() => setSplitMethod(m)} className={`rounded-lg py-1.5 ${splitMethod === m ? "bg-white font-medium" : ""}`}>
                {label}
              </button>
            ))}
          </div>
          {splitMethod !== "EQUAL" && (
            <div className="mt-3 space-y-2">
              {people.filter((p) => selected.includes(p.id)).map((p) => (
                <div key={p.id} className="flex items-center gap-2 text-sm">
                  <span className="flex-1">{p.displayName}</span>
                  <input className="w-28 rounded-xl border bg-white px-3 py-2 text-right" inputMode="decimal" placeholder={splitMethod === "SHARES" ? "1" : "0"} value={values[p.id] || ""} onChange={(e) => setValues({ ...values, [p.id]: e.target.value })} />
                  <span className="w-4 text-zinc-500">{splitMethod === "PERCENTAGE" ? "%" : splitMethod === "SHARES" ? "×" : "₹"}</span>
                </div>
              ))}
            </div>
          )}
        </div>
        <button className="w-full rounded-xl bg-zinc-900 py-3 text-white">{editId ? "Save changes" : "Add expense"}</button>
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
