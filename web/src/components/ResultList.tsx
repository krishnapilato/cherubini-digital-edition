import { useGSAP } from "@gsap/react";
import gsap from "gsap";
import { useEffect, useRef, type KeyboardEvent, type ReactNode } from "react";
import { find, type Query } from "../search";
import type { Lemma } from "../vocabulary";
import { Highlight, Snippet } from "./Highlight";

type Props = {
  items: Lemma[];
  selectedId: number | undefined;
  onSelect: (lemma: Lemma) => void;
  search: Query | null;
  toPrint: Set<number>;
  onTogglePrint: (id: number) => void;
  header: ReactNode;
  emptyMessage: string;
  animationKey: string;
};

export function ResultList({ items, selectedId, onSelect, search, toPrint, onTogglePrint, header, emptyMessage, animationKey }: Props) {
  const scope = useRef<HTMLElement>(null);

  useGSAP(
    () => {
      gsap.from(".row:nth-child(-n+12)", { y: 8, opacity: 0, duration: 0.3, stagger: 0.025, ease: "power2.out" });
    },
    { scope, dependencies: [animationKey] },
  );

  useEffect(() => {
    scope.current?.querySelector(".row.selected")?.scrollIntoView({ block: "nearest" });
  }, [selectedId]);

  const move = (event: KeyboardEvent<HTMLUListElement>) => {
    const step = event.key === "ArrowDown" ? 1 : event.key === "ArrowUp" ? -1 : 0;
    if (!step || items.length === 0) return;
    event.preventDefault();
    const index = items.findIndex((lemma) => lemma.id === selectedId);
    const next = items[Math.min(items.length - 1, Math.max(0, index + step))];
    onSelect(next);
    scope.current?.querySelector<HTMLButtonElement>(`[data-id="${next.id}"]`)?.focus();
  };

  return (
    <section className="card list-card" ref={scope} aria-label="Elenco">
      {header}
      {items.length === 0 ? (
        <p className="empty">{emptyMessage}</p>
      ) : (
        <ul className="rows" onKeyDown={move}>
          {items.map((lemma) => (
            <li key={lemma.id} className={lemma.id === selectedId ? "row selected" : "row"}>
              {search && (
                <input
                  type="checkbox"
                  className="check"
                  aria-label={`Da stampare: ${lemma.lemma}`}
                  title="Da stampare"
                  checked={toPrint.has(lemma.id)}
                  onChange={() => onTogglePrint(lemma.id)}
                />
              )}
              <button type="button" className="row-pick" data-id={lemma.id} aria-current={lemma.id === selectedId} onClick={() => onSelect(lemma)}>
                <span className="row-lemma">
                  <Highlight text={lemma.lemma} ranges={search ? find(lemma.searchableLemma, search) : []} />
                </span>
                <span className="row-snippet">
                  <Snippet text={lemma.definition} ranges={search ? find(lemma.searchableDefinition, search) : []} />
                </span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
