"use client";

import { api } from "@/lib/api";
import { Shell } from "@/components/Shell";
import Link from "next/link";
import { useEffect, useState } from "react";

type Group = { id: string; name: string; type: string };

export default function GroupsPage() {
  const [groups, setGroups] = useState<Group[]>([]);
  const [name, setName] = useState("");
  const [type, setType] = useState("FRIENDS");
  const [error, setError] = useState("");

  function load() {
    api("/api/groups").then(setGroups).catch((e) => setError(e.message));
  }

  useEffect(() => {
    load();
  }, []);

  async function create(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    try {
      const g = await api("/api/groups", { method: "POST", body: JSON.stringify({ name, type }) });
      setName("");
      setGroups((prev) => [g, ...prev]);
    } catch (err) {
      setError((err as Error).message);
    }
  }

  return (
    <Shell>
      <h1 className="text-2xl font-semibold">Groups</h1>
      <form onSubmit={create} className="mt-4 space-y-2 rounded-2xl bg-white p-4">
        <input className="w-full rounded-xl border border-zinc-300 px-3 py-2" placeholder="Group name" value={name} onChange={(e) => setName(e.target.value)} required />
        <select className="w-full rounded-xl border border-zinc-300 px-3 py-2" value={type} onChange={(e) => setType(e.target.value)}>
          {["FRIENDS", "FLATMATES", "TRIP", "FAMILY", "COLLEGE", "CUSTOM"].map((t) => (
            <option key={t}>{t}</option>
          ))}
        </select>
        <button className="w-full rounded-xl bg-zinc-900 py-2 text-white">Create group</button>
      </form>
      {error && <p className="mt-2 text-sm text-red-700">{error}</p>}
      <div className="mt-4 space-y-2">
        {groups.length === 0 && <p className="text-sm text-zinc-500">No groups yet. A group is just a shortcut for picking people.</p>}
        {groups.map((g) => (
          <Link key={g.id} href={`/groups/${g.id}`} className="block rounded-2xl bg-white px-4 py-3 font-medium">
            {g.name}
            <span className="ml-2 text-xs font-normal text-zinc-500">{g.type}</span>
          </Link>
        ))}
      </div>
    </Shell>
  );
}
