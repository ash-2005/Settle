import type { NextConfig } from "next";

// With NEXT_PUBLIC_API_URL="" the browser calls /api on its own origin and
// Next forwards it to the Spring API, so one public URL serves site + API.
const apiInternal = process.env.API_INTERNAL_URL || "http://api:8080";

const nextConfig: NextConfig = {
  output: "standalone",
  async rewrites() {
    return [{ source: "/api/:path*", destination: `${apiInternal}/api/:path*` }];
  },
};

export default nextConfig;
