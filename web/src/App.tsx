import { useGSAP } from "@gsap/react";
import gsap from "gsap";
import { CircleHelp, List, Printer } from "lucide-react";
import { useDeferredValue, useEffect, useMemo, useRef, useState, type KeyboardEvent } from "react";
import { Lente } from "./components/Lente";
import { LetterGrid } from "./components/LetterGrid";
import { AbbreviationsPanel, PrintPanel, PrintSheet } from "./components/Panels";
import { Reading } from "./components/Reading";
import { ResultList } from "./components/ResultList";
import { Toast } from "./components/Toast";
import { TopBar } from "./components/TopBar";
import { forgetSearch, loadRecent, rememberSearch } from "./history";
import { describe, find, makeQuery, matches } from "./search";
import { ALL, formatNumber, lemmas, lemmasOf, type Lemma } from "./vocabulary";

type Mode = "lente" | "atlante" | "strumento";
type Tab = "lemmi" | "definizioni";
type Panel = "abbreviazioni" | "stampa" | null;

const HELP_URL = `${import.meta.env.BASE_URL}guida/index.html`;

export default function App() {
  const [query, setQuery] = useState("");
  const [contained, setContained] = useState(false);
  const [letter, setLetter] = useState<string | null>(null);
  const [tab, setTab] = useState<Tab>("definizioni");
  const [selectedId, setSelectedId] = useState<number>();
  const [occurrence, setOccurrence] = useState({ id: -1, index: -1 });
  const [toPrint, setToPrint] = useState<Set<number>>(() => new Set());
  const [panel, setPanel] = useState<Panel>(null);
  const [toast, setToast] = useState<string | null>(null);
  const [recent, setRecent] = useState(loadRecent);
  const [readingOpen, setReadingOpen] = useState(false);

  const root = useRef<HTMLDivElement>(null);
  const lenteInput = useRef<HTMLInputElement>(null);
  const barInput = useRef<HTMLInputElement>(null);
  const previousMode = useRef<Mode>("lente");

  const typed = useMemo(() => makeQuery(query, !contained), [query, contained]);
  const mode: Mode = typed.words.length ? "strumento" : letter ? "atlante" : "lente";
  const deferredQuery = useDeferredValue(query);
  const search = useMemo(() => {
    const deferred = makeQuery(deferredQuery, !contained);
    return deferred.words.length ? deferred : null;
  }, [deferredQuery, contained]);

  const inLemmas = useMemo(() => (search ? lemmas.filter((lemma) => matches(lemma.searchableLemma, search)) : []), [search]);
  const inDefinitions = useMemo(() => (search ? lemmas.filter((lemma) => matches(lemma.searchableDefinition, search)) : []), [search]);
  const shownTab: Tab = tab === "lemmi" && !inLemmas.length && inDefinitions.length ? "definizioni"
    : tab === "definizioni" && !inDefinitions.length && inLemmas.length ? "lemmi" : tab;

  const activeSearch = mode === "strumento" ? search : null;
  const items = useMemo(
    () => (mode === "strumento" ? (shownTab === "lemmi" ? inLemmas : inDefinitions) : lemmasOf(letter ?? ALL)),
    [mode, shownTab, inLemmas, inDefinitions, letter],
  );
  const index = Math.max(0, items.findIndex((lemma) => lemma.id === selectedId));
  const selected: Lemma | null = items[index] ?? null;
  const definitionRanges = useMemo(() => (selected && activeSearch ? find(selected.searchableDefinition, activeSearch) : []), [selected, activeSearch]);
  const lemmaRanges = selected && activeSearch ? find(selected.searchableLemma, activeSearch) : [];
  const currentOccurrence = selected && occurrence.id === selected.id ? occurrence.index : -1;
  const chosen = useMemo(() => lemmas.filter((lemma) => toPrint.has(lemma.id)), [toPrint]);

  useEffect(() => {
    const before = previousMode.current;
    previousMode.current = mode;
    const target = mode === "lente" ? lenteInput.current : before === "lente" && mode === "strumento" ? barInput.current : null;
    if (!target || before === mode) return;
    target.focus();
    target.setSelectionRange(target.value.length, target.value.length);
  }, [mode]);

  useGSAP(
    () => {
      if (mode === "lente") return;
      gsap.from(".workspace .card", { y: 14, opacity: 0, duration: 0.45, stagger: 0.06, ease: "power3.out" });
    },
    { scope: root, dependencies: [mode] },
  );

  const goTo = (position: number) => {
    const target = items[Math.min(items.length - 1, Math.max(0, position))];
    if (target) setSelectedId(target.id);
  };

  const nextOccurrence = () => {
    if (!selected || !activeSearch) return;
    if (currentOccurrence + 1 < definitionRanges.length) {
      setOccurrence({ id: selected.id, index: currentOccurrence + 1 });
      return;
    }
    const following = items.slice(index + 1).find((lemma) => find(lemma.searchableDefinition, activeSearch).length > 0);
    if (!following) return;
    setSelectedId(following.id);
    setOccurrence({ id: following.id, index: 0 });
  };

  const previousOccurrence = () => {
    if (!selected || !definitionRanges.length) return;
    setOccurrence({ id: selected.id, index: currentOccurrence <= 0 ? definitionRanges.length - 1 : currentOccurrence - 1 });
  };

  const nextOccurrenceRef = useRef(nextOccurrence);
  const previousOccurrenceRef = useRef(previousOccurrence);
  useEffect(() => {
    nextOccurrenceRef.current = nextOccurrence;
    previousOccurrenceRef.current = previousOccurrence;
  });

  useEffect(() => {
    const onKey = (event: globalThis.KeyboardEvent) => {
      if (event.key !== "F3") return;
      event.preventDefault();
      if (event.shiftKey) previousOccurrenceRef.current();
      else nextOccurrenceRef.current();
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, []);

  const remember = (text = query) => {
    if (makeQuery(text, true).words.length) setRecent((saved) => rememberSearch(text, saved));
  };

  const onSearchKey = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === "Enter") remember();
    if (event.key === "Escape") setQuery("");
    if (event.key === "ArrowDown" && mode !== "lente") {
      event.preventDefault();
      const rows = root.current;
      (rows?.querySelector<HTMLButtonElement>(".row.selected .row-pick") ?? rows?.querySelector<HTMLButtonElement>(".row-pick"))?.focus();
    }
  };

  const select = (lemma: Lemma) => {
    setSelectedId(lemma.id);
    setReadingOpen(true);
    if (activeSearch) remember();
  };

  const togglePrint = (id: number) => {
    setToPrint((current) => {
      const next = new Set(current);
      if (!next.delete(id)) next.add(id);
      return next;
    });
  };

  const openLetter = (key: string) => {
    setLetter(key);
    setQuery("");
    setSelectedId(undefined);
  };

  const goHome = () => {
    setLetter(null);
    setQuery("");
    setReadingOpen(false);
  };

  const selectAllFound = () => {
    setToPrint((current) => new Set([...current, ...items.map((lemma) => lemma.id)]));
    setToast(`${formatNumber(items.length)} ${items.length === 1 ? "voce aggiunta" : "voci aggiunte"} alla stampa`);
  };

  const listHeader = mode === "strumento" ? (
    <div className="list-header">
      <div className="segmented" role="tablist">
        {(["lemmi", "definizioni"] as const).map((key) => {
          const count = key === "lemmi" ? inLemmas.length : inDefinitions.length;
          return (
            <button key={key} type="button" role="tab" aria-selected={shownTab === key} className={shownTab === key ? "selected" : ""} disabled={!count} onClick={() => setTab(key)}>
              {key === "lemmi" ? "Lemmi" : "Definizioni"}
              <span className="count">{formatNumber(count)}</span>
            </button>
          );
        })}
      </div>
      {search && <p className="summary">{describe(search)}</p>}
    </div>
  ) : (
    <div className="list-header atlante-header">
      <h2 className="list-title">{letter === ALL ? "Tutti i lemmi" : letter}</h2>
      <span className="list-count">{formatNumber(items.length)} lemmi</span>
    </div>
  );

  const emptyMessage = mode === "strumento"
    ? contained ? "Nessuna voce trovata. Controlla l'ortografia o usa meno parole." : "Nessuna voce trovata. Prova ad attivare «Parole contenute»."
    : "Nessun lemma per questa lettera.";

  return (
    <div className="app" ref={root}>
      {mode === "lente" ? (
        <>
          <nav className="lente-actions" aria-label="Azioni">
            <button type="button" className="button flat" onClick={() => setPanel("abbreviazioni")}>
              <List size={17} aria-hidden="true" />
              <span className="label">Abbreviazioni</span>
            </button>
            <button type="button" className="button flat" onClick={() => setPanel("stampa")}>
              <Printer size={17} aria-hidden="true" />
              <span className="label">Stampa</span>
              {toPrint.size > 0 && <span className="badge">{toPrint.size}</span>}
            </button>
            <a className="icon-button" href={HELP_URL} target="_blank" rel="noopener" aria-label="Guida all'uso" title="Guida all'uso">
              <CircleHelp size={20} />
            </a>
          </nav>
          <Lente
            query={query}
            onQuery={setQuery}
            onKeyDown={onSearchKey}
            inputRef={lenteInput}
            contained={contained}
            onContained={setContained}
            recent={recent}
            onRecent={(text) => {
              setQuery(text);
              remember(text);
            }}
            onForget={(text) => setRecent((saved) => forgetSearch(text, saved))}
            onLetter={openLetter}
          />
        </>
      ) : (
        <div className="workspace">
          <TopBar
            query={query}
            onQuery={setQuery}
            onKeyDown={onSearchKey}
            inputRef={barInput}
            contained={contained}
            onContained={setContained}
            onHome={goHome}
            onAbbreviations={() => setPanel("abbreviazioni")}
            onPrint={() => setPanel("stampa")}
            printCount={toPrint.size}
            helpUrl={HELP_URL}
          />
          {mode === "strumento" && typed.ignored.length > 0 && (
            <p className="warning" role="alert">
              Si cercano al massimo quattro parole: «{typed.ignored.join(" ")}» non viene considerato.
            </p>
          )}
          <main className={readingOpen ? `columns ${mode} reading-open` : `columns ${mode}`}>
            {mode === "atlante" && <LetterGrid letter={letter ?? ALL} onLetter={openLetter} />}
            <ResultList
              items={items}
              selectedId={selected?.id}
              onSelect={select}
              search={activeSearch}
              toPrint={toPrint}
              onTogglePrint={togglePrint}
              header={listHeader}
              emptyMessage={emptyMessage}
              animationKey={`${mode}|${letter}|${shownTab}|${deferredQuery}|${contained}`}
            />
            <Reading
              lemma={selected}
              index={selected ? index : -1}
              total={items.length}
              searching={mode === "strumento"}
              lemmaRanges={lemmaRanges}
              definitionRanges={definitionRanges}
              occurrence={currentOccurrence}
              onGo={goTo}
              onNextOccurrence={nextOccurrence}
              onPreviousOccurrence={previousOccurrence}
              printed={selected ? toPrint.has(selected.id) : false}
              onTogglePrint={() => selected && togglePrint(selected.id)}
              onBack={() => setReadingOpen(false)}
            />
          </main>
        </div>
      )}
      <footer className="statusbar">
        <span>{formatNumber(lemmas.length)} voci · sola consultazione</span>
        <span>A cura di Stefano Pilato e Khova Krishna Pilato</span>
      </footer>
      {panel === "abbreviazioni" && <AbbreviationsPanel onClosed={() => setPanel(null)} />}
      {panel === "stampa" && (
        <PrintPanel
          chosen={chosen}
          canSelectFound={mode === "strumento" && items.length > 0}
          onSelectFound={selectAllFound}
          onRemove={togglePrint}
          onClear={() => setToPrint(new Set())}
          onPrint={() => requestAnimationFrame(() => window.print())}
          onClosed={() => setPanel(null)}
        />
      )}
      {toast && <Toast message={toast} onDone={() => setToast(null)} />}
      <PrintSheet chosen={chosen} />
    </div>
  );
}
