import { CircleHelp, List, Printer } from "lucide-react";
import { quantity } from "../vocabulary";

const HELP_URL = `${import.meta.env.BASE_URL}guida/index.html`;

interface Props {
  readonly printCount: number;
  readonly onAbbreviations: () => void;
  readonly onPrint: () => void;
}

export function Actions({ printCount, onAbbreviations, onPrint }: Props) {
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
      <a className="icon-button" href={HELP_URL} target="_blank" rel="noopener" aria-label="Guida all'uso (si apre in una nuova scheda)" title="Guida all'uso">
        <CircleHelp size={20} aria-hidden="true" />
      </a>
    </>
  );
}
