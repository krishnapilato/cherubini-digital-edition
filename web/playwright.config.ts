import { defineConfig } from "@playwright/test";

const URL = "http://localhost:4174/cherubini-digital-edition/";
const CI = Boolean(process.env.CI);

// The interface tests run against the production build (npm run build) in the installed
// Chrome, so no browser has to be downloaded.
export default defineConfig({
  testDir: "e2e",
  testMatch: "*.e2e.ts",
  fullyParallel: true,
  forbidOnly: CI,
  retries: CI ? 1 : 0,
  reporter: CI ? "github" : "list",
  use: {
    baseURL: URL,
    channel: "chrome",
    viewport: { width: 1600, height: 900 },
  },
  webServer: {
    command: "npm run preview -- --port 4174 --strictPort",
    url: URL,
    reuseExistingServer: !CI,
  },
});
