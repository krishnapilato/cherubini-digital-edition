import "@fontsource/ibm-plex-sans/latin-400.css";
import "@fontsource/ibm-plex-sans/latin-500.css";
import "@fontsource/ibm-plex-sans/latin-600.css";
import { useGSAP } from "@gsap/react";
import gsap from "gsap";
import { StrictMode, Suspense } from "react";
import { createRoot } from "react-dom/client";
import App from "./App.tsx";
import { ErrorBoundary } from "./components/ErrorBoundary.tsx";
import "./index.css";

gsap.registerPlugin(useGSAP);

// The context menu is disabled everywhere, on purpose (see also user-select in index.css).
document.addEventListener("contextmenu", (event) => event.preventDefault());

const container = document.getElementById("root");
if (container) {
  createRoot(container).render(
    <StrictMode>
      <ErrorBoundary>
        <Suspense>
          <App />
        </Suspense>
      </ErrorBoundary>
    </StrictMode>,
  );
}
