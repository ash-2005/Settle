import type { Metadata } from "next";
import "./globals.css";
import { PwaRegister } from "@/components/PwaRegister";

export const metadata: Metadata = {
  title: "Settle",
  description: "Add it quickly. See exactly what happened. Settle with fewer transfers.",
  manifest: "/manifest.webmanifest",
  appleWebApp: { capable: true, title: "Settle", statusBarStyle: "default" },
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className="min-h-dvh bg-[#f6f5f2] antialiased">
        <PwaRegister />
        {children}
      </body>
    </html>
  );
}
