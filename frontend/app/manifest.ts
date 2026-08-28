import type { MetadataRoute } from "next";

export default function manifest(): MetadataRoute.Manifest {
  return {
    name: "Settle",
    short_name: "Settle",
    description: "Shared expenses and settlement",
    start_url: "/",
    display: "standalone",
    background_color: "#f6f5f2",
    theme_color: "#18181b",
    icons: [{ src: "/icon.svg", sizes: "any", type: "image/svg+xml", purpose: "any" }],
  };
}
