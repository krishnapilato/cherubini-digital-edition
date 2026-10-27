import { useGSAP } from "@gsap/react";
import gsap from "gsap";
import { ArrowLeft, ChevronDown, ChevronLeft, ChevronRight, ChevronUp, SkipBack, SkipForward } from "lucide-react";
import { useRef } from "react";
import type { Range } from "../search";
import { formatNumber, type Lemma } from "../vocabulary";
import { Highlight } from "./Highlight";

interface Props {
  readonly lemma: Lemma | null;
  readonly index: number;
  readonly total: number;
  readonly searching: boolean;
  readonly lemmaRanges: readonly Range[];
  readonly definitionRanges: readonly Range[];
  readonly occurrence: number;
  readonly onGo: (index: number) => void;
  readonly onNextOccurrence: () => void;
  readonly onPreviousOccurrence: () => void;
  readonly printed: boolean;
  readonly onTogglePrint: () => void;
  readonly onBack: () => void;
}

function describeOccurrence(count: number, occurrence: number): string {
  if (count === 0) return "Nessuna occorrenza";
  if (occurrence < 0) return `${count} ${count === 1 ? "occorrenza" : "occorrenze"}`;
  return `Occorrenza ${occurrence + 1} di ${count}`;
}

export function Reading(props: Props) {
  const scope = useRef<HTMLElement>(null);
  const { lemma, index, total, searching, definitionRanges, occurrence } = props;

  useGSAP(
    () => {
      const content = scope.current?.querySelector(".reading-content");
      if (content) gsap.from(content, { y: 10, opacity: 0, duration: 0.35, ease: "power2.out" });
    },
    { scope, dependencies: [lemma?.id], revertOnUpdate: true },
  );

  useGSAP(
    () => {
      const current = scope.current?.querySelector(".hit.current");
      if (!current) return;
      current.scrollIntoView({ block: "nearest" });
      gsap.from(current, { scale: 1.3, duration: 0.4, ease: "back.out(3)" });
    },
    { scope, dependencies: [occurrence, lemma?.id], revertOnUpdate: true },
  );

  const occurrenceLabel = describeOccurrence(definitionRanges.length, occurrence);

  return (
    <article className="card reading-card" ref={scope} aria-label="Voce">
      <div className="reading-toolbar">
        <button type="button" className="icon-button back" aria-label="Torna all'elenco" onClick={props.onBack}>
          <ArrowLeft size={18} aria-hidden="true" />
        </button>
        <button type="button" className="icon-button" aria-label="Prima voce" title="Prima voce" disabled={index <= 0} onClick={() => props.onGo(0)}>
          <SkipBack size={17} aria-hidden="true" />
        </button>
        <button type="button" className="icon-button" aria-label="Voce precedente" title="Voce precedente" disabled={index <= 0} onClick={() => props.onGo(index - 1)}>
          <ChevronLeft size={18} aria-hidden="true" />
        </button>
        <label className="position">
          <span>{searching ? "Voce" : "Lemma"}</span>
          <input
            key={index}
            aria-label="Posizione della voce"
            inputMode="numeric"
            defaultValue={total ? index + 1 : ""}
            onKeyDown={(event) => {
              if (event.key !== "Enter") return;
              const position = Number.parseInt(event.currentTarget.value, 10);
              if (position >= 1 && position <= total) props.onGo(position - 1);
              else event.currentTarget.value = String(index + 1);
            }}
          />
          <span>di {formatNumber(total)}</span>
        </label>
        <button type="button" className="icon-button" aria-label="Voce successiva" title="Voce successiva" disabled={index >= total - 1} onClick={() => props.onGo(index + 1)}>
          <ChevronRight size={18} aria-hidden="true" />
        </button>
        <button type="button" className="icon-button" aria-label="Ultima voce" title="Ultima voce" disabled={index >= total - 1} onClick={() => props.onGo(total - 1)}>
          <SkipForward size={17} aria-hidden="true" />
        </button>
        {searching && (
          <div className="occurrences">
            <span aria-live="polite">{occurrenceLabel}</span>
            <button type="button" className="icon-button" aria-label="Occorrenza precedente" title="Occorrenza precedente" disabled={definitionRanges.length === 0} onClick={props.onPreviousOccurrence}>
              <ChevronUp size={18} aria-hidden="true" />
            </button>
            <button type="button" className="button" title="Occorrenza successiva (F3)" disabled={definitionRanges.length === 0} onClick={props.onNextOccurrence}>
              <ChevronDown size={17} aria-hidden="true" />
              Successiva <kbd>F3</kbd>
            </button>
          </div>
        )}
      </div>
      <div className="reading-body">
        {lemma ? (
          <div className="reading-content">
            <p className="reading-meta">N° {lemma.id} · lettera {lemma.initial}</p>
            <h2 className="reading-lemma">
              <Highlight text={lemma.lemma} ranges={props.lemmaRanges} />
            </h2>
            <p className="reading-definition">
              <Highlight text={lemma.definition} ranges={definitionRanges} current={occurrence} />
            </p>
          </div>
        ) : (
          <p className="empty">Nessuna voce da mostrare.</p>
        )}
      </div>
      <footer className="reading-footer">
        <label className="check-label">
          <input type="checkbox" className="check" checked={props.printed} disabled={!lemma} onChange={props.onTogglePrint} />
          Da stampare
        </label>
        <span className="hint">{searching ? "F3 · occorrenza successiva" : "Frecce · voce precedente e successiva"}</span>
      </footer>
    </article>
  );
}
