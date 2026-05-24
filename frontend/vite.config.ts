import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";
import path from "path";

// https://vitejs.dev/config/
export default defineConfig({
  test: {
    globals: true,
    environment: "jsdom",
    setupFiles: "./src/test/setup.ts",
    css: true,
  },
  server: {
    host: "::",
    port: 5173,
    proxy: {
      "/api/notifications": { target: "http://localhost:8081", changeOrigin: true },
      "/api": { target: "http://localhost:8080", changeOrigin: true },
    },
    hmr: {
      overlay: false,
    },
  },
  plugins: [react()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
});
