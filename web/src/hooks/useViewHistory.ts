import { useEffect, useEffectEvent, useRef, useState } from "react";
import type { Mode } from "./useResults";

// The same width at which index.css shows the entry on its own screen.
const PHONE = "(max-width: 760px)";

const HOME = 0;
const LIST = 1;
const ENTRY = 2;

const currentDepth = (): number => (history.state as { depth?: number } | null)?.depth ?? HOME;

// Gives the browser's Back button something to do: from an entry open on a phone it returns
// to the list, and from the list to the start page, instead of leaving the site.
export function useViewHistory(mode: Mode, onHome: () => void) {
  const [readingOpen, setReadingOpen] = useState(() => currentDepth() === ENTRY);
  const leaving = useRef(false);

  const follow = useEffectEvent(() => {
    const depth = currentDepth();
    if (mode === "lente" && depth > HOME) {
      leaving.current = true;
      history.go(-depth);
    } else if (mode !== "lente" && depth === HOME) {
      history.pushState({ depth: LIST }, "");
    }
  });

  const onPop = useEffectEvent(() => {
    const depth = currentDepth();
    setReadingOpen(depth === ENTRY);
    if (leaving.current) {
      // The start page was reached inside the app: typing may already have left it again.
      leaving.current = false;
      follow();
    } else if (depth === HOME) {
      onHome();
    }
  });

  useEffect(() => follow(), [mode]);

  useEffect(() => {
    const listener = () => onPop();
    window.addEventListener("popstate", listener);
    return () => window.removeEventListener("popstate", listener);
  }, []);

  const openReading = () => {
    if (currentDepth() === LIST && matchMedia(PHONE).matches) history.pushState({ depth: ENTRY }, "");
    setReadingOpen(true);
  };

  const closeReading = () => {
    if (currentDepth() === ENTRY) history.back();
    else setReadingOpen(false);
  };

  return { readingOpen, openReading, closeReading };
}
