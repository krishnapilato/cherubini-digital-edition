import react from "@vitejs/plugin-react";
import { createHash } from "node:crypto";
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

// Everything comes from the site itself. GitHub Pages can't send headers, so the policy is a <meta>
// tag, added only to the build: the development server injects inline scripts of its own.
const POLICY = "default-src 'self'; object-src 'none'; base-uri 'self'; form-action 'none'";

const policyTag = (policy: string) => ({
  tag: "meta",
  attrs: { "http-equiv": "Content-Security-Policy", content: policy },
  injectTo: "head-prepend" as const,
});

// The vocabulary JSON is fetched by the app; preloading it from the HTML downloads it
// in parallel with the JavaScript instead of after it. After a reload the app reads it
// from sessionStorage (see vocabulary.ts), and an unused preload is a console warning,
// so the link is only added when that copy is missing. The policy allows that one inline
// script by its hash.
function preloadVocabulary(): Plugin {
  return {
    name: "preload-vocabulary",
    transformIndexHtml: {
      order: "post",
      handler: (_, context) => {
        const asset = Object.values(context.bundle ?? {}).find(
          (file) => file.type === "asset" && /^assets\/cherubini-.*\.json$/.test(file.fileName),
        );
        if (!asset) return [];
        const link = JSON.stringify({
          rel: "preload",
          as: "fetch",
          type: "application/json",
          crossOrigin: "anonymous",
          href: `${BASE}${asset.fileName}`,
        });
        const script = `try{if(!sessionStorage.getItem("cherubini.vocabulary"))document.head.append(Object.assign(document.createElement("link"),${link}))}catch{}`;
        const hash = createHash("sha256").update(script).digest("base64");
        return [
          policyTag(`${POLICY}; script-src 'self' 'sha256-${hash}'`),
          { tag: "script", children: script, injectTo: "head" },
        ];
      },
    },
  };
}

// The help guide lives in ../guide, shared with the desktop edition. It is served at
// /guida/ during development and copied to dist/guida/ by the build, where its page gets
// the same policy: the source can't carry it, because the desktop opens the guide from
// file://, where 'self' is unreliable.
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
      const meta = `<meta http-equiv="Content-Security-Policy" content="${POLICY}">\n  <meta name="referrer" content="no-referrer">`;
      for (const name of readdirSync(GUIDE)) {
        const content = readFileSync(join(GUIDE, name));
        const source =
          name === "index.html"
            ? content.toString("utf8").replace(/<meta charset="utf-8">/i, (charset) => `${charset}\n  ${meta}`)
            : content;
        this.emitFile({ type: "asset", fileName: `guida/${name}`, source });
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
