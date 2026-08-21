"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const items = [
  { href: "/", label: "Home" },
  { href: "/groups", label: "Groups" },
  { href: "/activity", label: "Activity" },
  { href: "/profile", label: "Profile" },
];

export function Shell({ children }: { children: React.ReactNode }) {
  const path = usePathname();
  return (
    <div className="mx-auto flex min-h-dvh max-w-md flex-col bg-[#f6f5f2] text-zinc-900">
      <main className="flex-1 px-4 pb-24 pt-6">{children}</main>
      <nav className="fixed bottom-0 left-1/2 z-10 flex w-full max-w-md -translate-x-1/2 border-t border-zinc-200 bg-white">
        {items.map((item) => {
          const active = path === item.href || (item.href !== "/" && path.startsWith(item.href));
          return (
            <Link
              key={item.href}
              href={item.href}
              className={`flex-1 py-3 text-center text-sm ${active ? "font-semibold text-zinc-900" : "text-zinc-500"}`}
            >
              {item.label}
            </Link>
          );
        })}
      </nav>
      <Link
        href="/expenses/new"
        className="fixed bottom-20 right-[max(1rem,calc(50%-11.5rem))] z-20 flex h-14 w-14 items-center justify-center rounded-full bg-zinc-900 text-2xl text-white shadow-lg"
        aria-label="Add expense"
      >
        +
      </Link>
    </div>
  );
}
