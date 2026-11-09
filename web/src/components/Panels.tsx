import { Printer, X } from "lucide-react";
import { quantity, type Lemma } from "../vocabulary";
import { Modal } from "./Modal";

const HIDDEN = { x: 48, opacity: 0 };

interface PrintProps {
  readonly chosen: readonly Lemma[];
  readonly canSelectFound: boolean;
  readonly onSelectFound: () => void;
  readonly onRemove: (id: number) => void;
  readonly onClear: () => void;
  readonly onPrint: () => void;
  readonly onClosed: () => void;
}

export function PrintPanel({
  chosen,
  canSelectFound,
  onSelectFound,
  onRemove,
  onClear,
  onPrint,
  onClosed,
}: PrintProps) {
  const count = chosen.length;
  return (
    <Modal
      className="panel"
      hidden={HIDDEN}
      title="Stampa"
      subtitle={count === 0 ? "Nessuna voce scelta" : quantity(count, "voce scelta", "voci scelte")}
      onClosed={onClosed}
    >
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
                  <button
                    type="button"
                    className="icon-button"
                    aria-label={`Togli ${lemma.lemma} dalla stampa`}
                    title="Togli dalla stampa"
                    onClick={() => onRemove(lemma.id)}
                  >
                    <X size={16} aria-hidden="true" />
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
              {count === 0 ? "Stampa" : `Stampa ${quantity(count, "voce", "voci")}`}
            </button>
            <p className="note">Si apre la finestra di stampa del browser per scegliere la stampante.</p>
          </footer>
        </>
      )}
    </Modal>
  );
}

export function PrintSheet({ chosen }: Readonly<{ chosen: readonly Lemma[] }>) {
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
