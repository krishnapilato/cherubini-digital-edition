import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

export default defineConfig({
  base: "/cherubini-digital-edition/",
  plugins: [react()],
  server: {
    fs: { allow: [".."] },
  },
  build: {
    chunkSizeWarningLimit: 800,
  },
});
