import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Settle",
  description: "Add it quickly. See exactly what happened. Settle with fewer transfers.",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className="min-h-dvh bg-[#f6f5f2] antialiased">{children}</body>
    </html>
  );
}
