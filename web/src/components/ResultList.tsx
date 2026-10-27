import { useGSAP } from "@gsap/react";
import { useVirtualizer } from "@tanstack/react-virtual";
import gsap from "gsap";
import { useEffect, useRef, type KeyboardEvent, type ReactNode } from "react";
import { find, type Query } from "../search";
import type { Lemma } from "../vocabulary";
import { Highlight, Snippet } from "./Highlight";

interface Props {
  readonly items: readonly Lemma[];
  readonly selectedId: number | undefined;
  readonly onSelect: (lemma: Lemma) => void;
  readonly search: Query | null;
  readonly toPrint: ReadonlySet<number>;
  readonly onTogglePrint: (id: number) => void;
  readonly header: ReactNode;
  readonly emptyMessage: string;
  readonly animationKey: string;
}

interface RowProps {
  readonly lemma: Lemma;
  readonly search: Query | null;
  readonly selected: boolean;
  readonly printed: boolean;
  readonly onSelect: (lemma: Lemma) => void;
  readonly onTogglePrint: (id: number) => void;
  readonly onKeyDown: (event: KeyboardEvent<HTMLButtonElement>) => void;
}

function Row({ lemma, search, selected, printed, onSelect, onTogglePrint, onKeyDown }: RowProps) {
  return (
    <>
      {search && (
        <input type="checkbox" className="check" aria-label={`Da stampare: ${lemma.lemma}`} title="Da stampare" checked={printed} onChange={() => onTogglePrint(lemma.id)} />
      )}
      <button type="button" className="row-pick" data-id={lemma.id} aria-current={selected ? "true" : undefined} onClick={() => onSelect(lemma)} onKeyDown={onKeyDown}>
        <span className="row-lemma">
          <Highlight text={lemma.lemma} ranges={search ? find(lemma.searchableLemma, search) : []} />
        </span>
        <span className="row-snippet">
          <Snippet text={lemma.definition} ranges={search ? find(lemma.searchableDefinition, search) : []} />
        </span>
      </button>
    </>
  );
}

export function ResultList({ items, selectedId, onSelect, search, toPrint, onTogglePrint, header, emptyMessage, animationKey }: Props) {
  const scope = useRef<HTMLElement>(null);
  const scroller = useRef<HTMLDivElement>(null);
  const selectedIndex = items.findIndex((lemma) => lemma.id === selectedId);

  const virtualizer = useVirtualizer({
    count: items.length,
    getScrollElement: () => scroller.current,
    estimateSize: () => 66,
    getItemKey: (index) => items[index]?.id ?? index,
    overscan: 6,
  });

  // The virtual rows appear only after the list has been measured, so the entrance
  // animation waits for the next frame.
  useGSAP(
    (_, contextSafe) => {
      scroller.current?.scrollTo({ top: 0 });
      const animate = contextSafe?.(() => {
        const rows = scroller.current?.querySelectorAll(".row");
        if (rows?.length) gsap.from(rows, { y: 8, opacity: 0, duration: 0.3, stagger: 0.025, ease: "power2.out" });
      });
      if (!animate) return;
      const frame = requestAnimationFrame(animate);
      return () => cancelAnimationFrame(frame);
    },
    { scope, dependencies: [animationKey], revertOnUpdate: true },
  );

  // Waits one frame so the list has its real height before scrolling to the selection;
  // on the first render (for example after a reload) the height is still zero.
  useEffect(() => {
    if (selectedIndex < 0) return;
    const frame = requestAnimationFrame(() => virtualizer.scrollToIndex(selectedIndex, { align: "auto" }));
    return () => cancelAnimationFrame(frame);
  }, [selectedIndex, virtualizer]);

  const move = (event: KeyboardEvent<HTMLButtonElement>) => {
    const steps: Partial<Record<string, number>> = { ArrowDown: 1, ArrowUp: -1, PageDown: 10, PageUp: -10 };
    const step = steps[event.key];
    if (step === undefined || items.length === 0) return;
    event.preventDefault();
    const next = items[Math.min(items.length - 1, Math.max(0, selectedIndex + step))];
    if (!next) return;
    onSelect(next);
    requestAnimationFrame(() => scroller.current?.querySelector<HTMLButtonElement>(`[data-id="${next.id}"]`)?.focus());
  };

  return (
    <section className="card list-card" ref={scope} aria-label="Elenco">
      {header}
      {items.length === 0 ? (
        <p className="empty">{emptyMessage}</p>
      ) : (
        <div className="rows" ref={scroller}>
          <ul style={{ height: virtualizer.getTotalSize() }}>
            {virtualizer.getVirtualItems().map((row) => {
              const lemma = items[row.index];
              if (!lemma) return null;
              return (
                <li
                  key={row.key}
                  ref={virtualizer.measureElement}
                  data-index={row.index}
                  className={lemma.id === selectedId ? "row selected" : "row"}
                  style={{ top: row.start }}
                  aria-setsize={items.length}
                  aria-posinset={row.index + 1}
                >
                  <Row
                    lemma={lemma}
                    search={search}
                    selected={lemma.id === selectedId}
                    printed={toPrint.has(lemma.id)}
                    onSelect={onSelect}
                    onTogglePrint={onTogglePrint}
                    onKeyDown={move}
                  />
                </li>
              );
            })}
          </ul>
        </div>
      )}
    </section>
  );
}
