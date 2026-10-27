import { useEffect, useEffectEvent, useMemo, useState } from "react";
import { find, type Query, type Range } from "../search";
import { useSession } from "../store";
import type { Lemma } from "../vocabulary";

export interface Occurrences {
  readonly ranges: readonly Range[];
  readonly current: number;
  readonly next: () => void;
  readonly previous: () => void;
}

const NONE = { id: -1, index: -1 };

// Moves through the highlighted words of the open entry; past the last one it jumps
// to the next entry that contains the search. F3 and Shift+F3 work anywhere on the page.
export function useOccurrences(items: readonly Lemma[], selected: Lemma | null, index: number, search: Query | null): Occurrences {
  const select = useSession((state) => state.select);
  const [occurrence, setOccurrence] = useState(NONE);

  const ranges = useMemo(() => (selected && search ? find(selected.searchableDefinition, search) : []), [selected, search]);
  const current = selected?.id === occurrence.id ? occurrence.index : -1;

  const next = () => {
    if (!selected || !search) return;
    if (current + 1 < ranges.length) {
      setOccurrence({ id: selected.id, index: current + 1 });
      return;
    }
    const following = items.slice(index + 1).find((lemma) => find(lemma.searchableDefinition, search).length > 0);
    if (!following) return;
    select(following.id);
    setOccurrence({ id: following.id, index: 0 });
  };

  const previous = () => {
    if (!selected || ranges.length === 0) return;
    setOccurrence({ id: selected.id, index: current <= 0 ? ranges.length - 1 : current - 1 });
  };

  const onKey = useEffectEvent((event: KeyboardEvent) => {
    if (event.key !== "F3") return;
    event.preventDefault();
    if (event.shiftKey) previous();
    else next();
  });

  useEffect(() => {
    const listener = (event: KeyboardEvent) => onKey(event);
    window.addEventListener("keydown", listener);
    return () => window.removeEventListener("keydown", listener);
  }, []);

  return { ranges, current, next, previous };
}
