import { CircleHelp, List, Printer } from "lucide-react";
import { quantity } from "../vocabulary";

interface Props {
  readonly printCount: number;
  readonly onAbbreviations: () => void;
  readonly onPrint: () => void;
  readonly onHelp: () => void;
}

export function Actions({ printCount, onAbbreviations, onPrint, onHelp }: Props) {
  return (
    <>
      <button type="button" className="button flat" aria-label="Abbreviazioni" onClick={onAbbreviations}>
        <List size={17} aria-hidden="true" />
        <span className="label">Abbreviazioni</span>
      </button>
      <button type="button" className="button flat" aria-label={printCount > 0 ? `Stampa, ${quantity(printCount, "voce scelta", "voci scelte")}` : "Stampa"} onClick={onPrint}>
        <Printer size={17} aria-hidden="true" />
        <span className="label">Stampa</span>
        {printCount > 0 && (
          <span className="badge" aria-hidden="true">
            {printCount}
          </span>
        )}
      </button>
      <button type="button" className="icon-button" aria-label="Guida all'uso" title="Guida all'uso" onClick={onHelp}>
        <CircleHelp size={20} aria-hidden="true" />
      </button>
    </>
  );
}
