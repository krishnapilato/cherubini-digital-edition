import { useEffect, useEffectEvent } from "react";

// As the hint under the entry says, the arrows move to the previous or next entry from
// anywhere on the page. The list rows handle them on their own (and prevent the default),
// text fields keep them for the cursor and open dialogs keep them for themselves.
export function useArrowKeys(active: boolean, onStep: (step: number) => void): void {
  const onKey = useEffectEvent((event: KeyboardEvent) => {
    const step = { ArrowDown: 1, ArrowUp: -1 }[event.key];
    if (!active || step === undefined || event.defaultPrevented || event.altKey || event.ctrlKey || event.metaKey)
      return;
    if (event.target instanceof Element && event.target.closest("input, textarea, select, [contenteditable]")) return;
    if (document.querySelector("dialog[open]")) return;
    event.preventDefault();
    onStep(step);
  });

  useEffect(() => {
    const listener = (event: KeyboardEvent) => onKey(event);
    window.addEventListener("keydown", listener);
    return () => window.removeEventListener("keydown", listener);
  }, []);
}
