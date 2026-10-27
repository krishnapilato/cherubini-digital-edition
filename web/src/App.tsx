import { useGSAP } from "@gsap/react";
import gsap from "gsap";
import { RotateCw } from "lucide-react";
import { use, useEffect, useMemo, useRef, useState, type KeyboardEvent } from "react";
import { Actions } from "./components/Actions";
import { Lente } from "./components/Lente";
import { LetterGrid } from "./components/LetterGrid";
import { ListHeader } from "./components/ListHeader";
import { AbbreviationsPanel, PrintPanel, PrintSheet } from "./components/Panels";
import { Reading } from "./components/Reading";
import { ResultList } from "./components/ResultList";
import { Toast } from "./components/Toast";
import { TopBar } from "./components/TopBar";
import { useOccurrences } from "./hooks/useOccurrences";
import { useResults, type Mode } from "./hooks/useResults";
import { find } from "./search";
import { useSession } from "./store";
import { formatNumber, quantity, vocabularyReady, type Lemma, type Vocabulary } from "./vocabulary";

type Panel = "abbreviazioni" | "stampa" | null;

export default function App() {
  const vocabulary = use(vocabularyReady);
  return vocabulary ? <Explorer vocabulary={vocabulary} /> : <LoadError />;
}

function LoadError() {
  return (
    <main className="load-error" role="alert">
      <h1>Il vocabolario non si è caricato</h1>
      <p>Controlla la connessione e ricarica la pagina.</p>
      <button type="button" className="button accent" onClick={() => location.reload()}>
        <RotateCw size={17} aria-hidden="true" />
        Ricarica
      </button>
    </main>
  );
}

function emptyMessageFor(mode: Mode, contained: boolean): string {
  if (mode !== "strumento") return "Nessun lemma per questa lettera.";
  return contained ? "Nessuna voce trovata. Controlla l'ortografia o usa meno parole." : "Nessuna voce trovata. Prova ad attivare «Parole contenute».";
}

// Moves the caret into the search field that becomes visible when the view changes,
// so typing can continue without clicking.
function useSearchFocus(mode: Mode) {
  const lenteInput = useRef<HTMLInputElement>(null);
  const barInput = useRef<HTMLInputElement>(null);
  const previousMode = useRef<Mode | null>(null);

  useEffect(() => {
    const before = previousMode.current;
    previousMode.current = mode;
    if (before === mode || mode === "atlante") return;
    if (before === null && mode !== "lente") return;
    const target = mode === "lente" ? lenteInput.current : barInput.current;
    target?.focus();
    target?.setSelectionRange(target.value.length, target.value.length);
  }, [mode]);

  return { lenteInput, barInput };
}

function Explorer({ vocabulary }: Readonly<{ vocabulary: Vocabulary }>) {
  const results = useResults(vocabulary);
  const { mode, typed, search, items } = results;
  const query = useSession((state) => state.query);
  const contained = useSession((state) => state.contained);
  const selectedId = useSession((state) => state.selectedId);
  const printIds = useSession((state) => state.toPrint);
  const { setQuery, select, togglePrint, addToPrint, clearPrint, remember, goHome } = useSession.getState();

  const [panel, setPanel] = useState<Panel>(null);
  const [toast, setToast] = useState<string | null>(null);
  const [readingOpen, setReadingOpen] = useState(false);
  const root = useRef<HTMLDivElement>(null);
  const { lenteInput, barInput } = useSearchFocus(mode);

  const index = Math.max(0, items.findIndex((lemma) => lemma.id === selectedId));
  const selected: Lemma | null = items[index] ?? null;
  const occurrences = useOccurrences(items, selected, index, search);
  const toPrint = useMemo(() => new Set(printIds), [printIds]);
  const chosen = useMemo(() => vocabulary.lemmas.filter((lemma) => toPrint.has(lemma.id)), [toPrint, vocabulary]);
  const countOf = (letter: string) => vocabulary.byLetter.get(letter)?.length ?? 0;

  useGSAP(
    () => {
      if (mode !== "lente") gsap.from(".workspace .card", { y: 14, opacity: 0, duration: 0.45, stagger: 0.06, ease: "power3.out" });
    },
    { scope: root, dependencies: [mode], revertOnUpdate: true },
  );

  const onSearchKey = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === "Enter") remember(query);
    if (event.key === "Escape") setQuery("");
    if (event.key !== "ArrowDown" || mode === "lente") return;
    event.preventDefault();
    const list = root.current;
    const row = list?.querySelector<HTMLButtonElement>(".row.selected .row-pick") ?? list?.querySelector<HTMLButtonElement>(".row-pick");
    row?.focus();
  };

  const open = (lemma: Lemma) => {
    select(lemma.id);
    setReadingOpen(true);
    if (search) remember(query);
  };

  const goTo = (position: number) => {
    const target = items[Math.min(items.length - 1, Math.max(0, position))];
    if (target) select(target.id);
  };

  const selectAllFound = () => {
    addToPrint(items.map((lemma) => lemma.id));
    setToast(`${quantity(items.length, "voce aggiunta", "voci aggiunte")} alla stampa`);
  };

  const actions = <Actions printCount={toPrint.size} onAbbreviations={() => setPanel("abbreviazioni")} onPrint={() => setPanel("stampa")} />;

  return (
    <div className="app" ref={root}>
      {mode === "lente" ? (
        <>
          <nav className="lente-actions" aria-label="Azioni">
            {actions}
          </nav>
          <Lente onKeyDown={onSearchKey} inputRef={lenteInput} countOf={countOf} />
        </>
      ) : (
        <div className="workspace">
          <TopBar
            onKeyDown={onSearchKey}
            inputRef={barInput}
            onHome={() => {
              goHome();
              setReadingOpen(false);
            }}
          >
            {actions}
          </TopBar>
          {typed.ignored.length > 0 && (
            <p className="warning" role="alert">
              Si cercano al massimo quattro parole: «{typed.ignored.join(" ")}» non viene considerato.
            </p>
          )}
          <main className={`columns ${mode}${readingOpen ? " reading-open" : ""}`}>
            {mode === "atlante" && <LetterGrid countOf={countOf} />}
            <ResultList
              items={items}
              selectedId={selected?.id}
              onSelect={open}
              search={search}
              toPrint={toPrint}
              onTogglePrint={togglePrint}
              header={<ListHeader results={results} />}
              emptyMessage={emptyMessageFor(mode, contained)}
              animationKey={results.searchKey}
            />
            <Reading
              lemma={selected}
              index={selected ? index : -1}
              total={items.length}
              searching={mode === "strumento"}
              lemmaRanges={selected && search ? find(selected.searchableLemma, search) : []}
              definitionRanges={occurrences.ranges}
              occurrence={occurrences.current}
              onGo={goTo}
              onNextOccurrence={occurrences.next}
              onPreviousOccurrence={occurrences.previous}
              printed={selected ? toPrint.has(selected.id) : false}
              onTogglePrint={() => {
                if (selected) togglePrint(selected.id);
              }}
              onBack={() => setReadingOpen(false)}
            />
          </main>
        </div>
      )}
      <footer className="statusbar">
        <span>{formatNumber(vocabulary.lemmas.length)} voci · sola consultazione</span>
        <span>A cura di Stefano Pilato e Khova Krishna Pilato</span>
      </footer>
      {panel === "abbreviazioni" && <AbbreviationsPanel onClosed={() => setPanel(null)} />}
      {panel === "stampa" && (
        <PrintPanel
          chosen={chosen}
          canSelectFound={mode === "strumento" && items.length > 0}
          onSelectFound={selectAllFound}
          onRemove={togglePrint}
          onClear={clearPrint}
          onPrint={() => requestAnimationFrame(() => window.print())}
          onClosed={() => setPanel(null)}
        />
      )}
      {toast && <Toast message={toast} onDone={() => setToast(null)} />}
      <PrintSheet chosen={chosen} />
    </div>
  );
}
