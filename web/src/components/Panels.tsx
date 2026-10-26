import { List, Printer, X } from "lucide-react";
import { useState } from "react";
import type { Lemma } from "../vocabulary";
import { SidePanel } from "./SidePanel";

const CATEGORIES = [
  ["Autori", "Autori"],
  ["Abbrev.", "Abbreviazioni"],
  ["Scritt.", "Scrittori"],
  ["Contr.", "Contrazioni"],
] as const;

export function AbbreviationsPanel({ onClosed }: { onClosed: () => void }) {
  const [category, setCategory] = useState<string>(CATEGORIES[0][1]);
  return (
    <SidePanel title="Abbreviazioni" subtitle="usate nel Vocabolario Cherubini" onClosed={onClosed}>
      {() => (
        <>
          <div className="segmented">
            {CATEGORIES.map(([label, name]) => (
              <button key={name} type="button" className={name === category ? "selected" : ""} aria-pressed={name === category} onClick={() => setCategory(name)}>
                {label}
              </button>
            ))}
          </div>
          <div className="empty-state">
            <span className="empty-icon">
              <List size={22} aria-hidden="true" />
            </span>
            <h3>Elenco non ancora disponibile</h3>
            <p>Le abbreviazioni del Cherubini ({category}) compariranno qui con la loro spiegazione e la ricerca nel vocabolario.</p>
          </div>
        </>
      )}
    </SidePanel>
  );
}

type PrintProps = {
  chosen: Lemma[];
  canSelectFound: boolean;
  onSelectFound: () => void;
  onRemove: (id: number) => void;
  onClear: () => void;
  onPrint: () => void;
  onClosed: () => void;
};

export function PrintPanel({ chosen, canSelectFound, onSelectFound, onRemove, onClear, onPrint, onClosed }: PrintProps) {
  const count = chosen.length;
  return (
    <SidePanel title="Stampa" subtitle={count === 0 ? "Nessuna voce scelta" : `${count} ${count === 1 ? "voce scelta" : "voci scelte"}`} onClosed={onClosed}>
      {(close) => (
        <>
          {count === 0 ? (
            <p className="empty">Nessuna voce scelta. Spunta «Da stampare» sulle voci trovate o nella voce aperta.</p>
          ) : (
            <ul className="print-list">
              {chosen.map((lemma) => (
                <li key={lemma.id}>
                  <div>
                    <strong>{lemma.lemma}</strong>
                    <small>N° {lemma.id}</small>
                  </div>
                  <button type="button" className="icon-button" aria-label={`Togli ${lemma.lemma} dalla stampa`} title="Togli dalla stampa" onClick={() => onRemove(lemma.id)}>
                    <X size={16} />
                  </button>
                </li>
              ))}
            </ul>
          )}
          <footer className="panel-footer">
            <div className="panel-row">
              <button type="button" className="button" disabled={!canSelectFound} onClick={onSelectFound}>
                Tutte le voci trovate
              </button>
              <button type="button" className="button quiet" disabled={count === 0} onClick={onClear}>
                Svuota
              </button>
            </div>
            <button
              type="button"
              className="button accent wide"
              disabled={count === 0}
              onClick={() => {
                close();
                onPrint();
              }}
            >
              <Printer size={17} aria-hidden="true" />
              {count === 0 ? "Stampa" : `Stampa ${count} ${count === 1 ? "voce" : "voci"}`}
            </button>
            <p className="note">Si apre la finestra di stampa del browser per scegliere la stampante.</p>
          </footer>
        </>
      )}
    </SidePanel>
  );
}

export function PrintSheet({ chosen }: { chosen: Lemma[] }) {
  return (
    <section className="print-sheet" aria-hidden="true">
      <h1>Vocabolario milanese–italiano di Francesco Cherubini</h1>
      {chosen.map((lemma) => (
        <p key={lemma.id}>
          <strong>{lemma.lemma}</strong> {lemma.definition}
        </p>
      ))}
    </section>
  );
}
