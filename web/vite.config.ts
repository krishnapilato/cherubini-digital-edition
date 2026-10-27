import react from "@vitejs/plugin-react";
import { readdirSync, readFileSync } from "node:fs";
import { extname, join } from "node:path";
import { defineConfig, type Plugin } from "vite";

const BASE = "/cherubini-digital-edition/";
const GUIDE = join(import.meta.dirname, "..", "guide");
const GUIDE_PATH = `${BASE}guida/`;

const CONTENT_TYPES: Readonly<Record<string, string>> = {
  ".html": "text/html; charset=utf-8",
  ".css": "text/css; charset=utf-8",
  ".js": "text/javascript; charset=utf-8",
  ".png": "image/png",
  ".ttf": "font/ttf",
  ".txt": "text/plain; charset=utf-8",
};

// The vocabulary JSON is fetched by the app; preloading it from the HTML downloads it
// in parallel with the JavaScript instead of after it.
function preloadVocabulary(): Plugin {
  return {
    name: "preload-vocabulary",
    transformIndexHtml: {
      order: "post",
      handler: (_, context) => {
        const asset = Object.values(context.bundle ?? {}).find((file) => file.type === "asset" && /^assets\/cherubini-.*\.json$/.test(file.fileName));
        if (!asset) return [];
        return [
          {
            tag: "link",
            attrs: { rel: "preload", as: "fetch", type: "application/json", crossorigin: "anonymous", href: `${BASE}${asset.fileName}` },
            injectTo: "head",
          },
        ];
      },
    },
  };
}

// The help guide lives in ../guide, shared with the desktop edition. It is served at
// /guida/ during development and copied to dist/guida/ by the build.
function guide(): Plugin {
  return {
    name: "guide",
    configureServer: (server) => {
      server.middlewares.use(GUIDE_PATH, (request, response, next) => {
        const name = decodeURIComponent((request.url ?? "/").split("?")[0] ?? "/").replace(/^\/$/, "/index.html");
        const type = CONTENT_TYPES[extname(name)];
        if (!type || name.includes("..")) {
          next();
          return;
        }
        try {
          const content = readFileSync(join(GUIDE, name));
          response.setHeader("Content-Type", type);
          response.end(content);
        } catch {
          next();
        }
      });
    },
    generateBundle() {
      for (const name of readdirSync(GUIDE)) {
        this.emitFile({ type: "asset", fileName: `guida/${name}`, source: readFileSync(join(GUIDE, name)) });
      }
    },
  };
}

export default defineConfig({
  base: BASE,
  plugins: [react(), preloadVocabulary(), guide()],
  server: {
    fs: { allow: [".."] },
  },
});
