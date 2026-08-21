"use client";

import { api, clearTokens } from "@/lib/api";
import { Shell } from "@/components/Shell";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function ProfilePage() {
  const router = useRouter();
  const [me, setMe] = useState<{ displayName: string; username: string; phone: string } | null>(null);

  useEffect(() => {
    api("/api/me").then(setMe).catch(() => router.replace("/login"));
  }, [router]);

  return (
    <Shell>
      <h1 className="text-2xl font-semibold">Profile</h1>
      {me && (
        <div className="mt-4 rounded-2xl bg-white p-4 text-sm">
          <p className="text-lg font-medium">{me.displayName}</p>
          <p className="text-zinc-500">@{me.username}</p>
          <p className="mt-2 text-zinc-500">{me.phone}</p>
        </div>
      )}
      <p className="mt-4 text-sm text-zinc-500">Local OTP is 123456. This is a student project, not a bank.</p>
      <button
        className="mt-6 w-full rounded-xl border border-zinc-300 py-2"
        onClick={() => {
          clearTokens();
          router.replace("/login");
        }}
      >
        Sign out
      </button>
    </Shell>
  );
}
