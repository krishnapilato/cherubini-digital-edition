import { CircleHelp, List, Printer, Search, X } from "lucide-react";
import type { KeyboardEvent, RefObject } from "react";
import icon from "../assets/cherubini.svg";
import { Switch } from "./Switch";

type Props = {
  query: string;
  onQuery: (query: string) => void;
  onKeyDown: (event: KeyboardEvent<HTMLInputElement>) => void;
  inputRef: RefObject<HTMLInputElement | null>;
  contained: boolean;
  onContained: (contained: boolean) => void;
  onHome: () => void;
  onAbbreviations: () => void;
  onPrint: () => void;
  printCount: number;
  helpUrl: string;
};

export function TopBar(props: Props) {
  return (
    <header className="topbar">
      <button type="button" className="home" onClick={props.onHome} title="Pagina iniziale">
        <img src={icon} alt="" width="30" height="30" />
        <span>Cherubini</span>
      </button>
      <label className="bar-search">
        <Search size={17} aria-hidden="true" />
        <input
          ref={props.inputRef}
          aria-label="Cerca nel vocabolario"
          placeholder="Cerca un lemma o fino a quattro parole"
          value={props.query}
          onChange={(event) => props.onQuery(event.target.value)}
          onKeyDown={props.onKeyDown}
        />
        {props.query && (
          <button type="button" className="icon-button small" aria-label="Azzera la ricerca" title="Azzera (Esc)" onClick={() => props.onQuery("")}>
            <X size={15} />
          </button>
        )}
      </label>
      <Switch checked={props.contained} onChange={props.onContained} label="Parole contenute" />
      <div className="topbar-actions">
        <button type="button" className="button" onClick={props.onAbbreviations}>
          <List size={17} aria-hidden="true" />
          <span className="label">Abbreviazioni</span>
        </button>
        <button type="button" className="button" onClick={props.onPrint}>
          <Printer size={17} aria-hidden="true" />
          <span className="label">Stampa</span>
          {props.printCount > 0 && <span className="badge">{props.printCount}</span>}
        </button>
        <a className="icon-button" href={props.helpUrl} target="_blank" rel="noopener" aria-label="Guida all'uso" title="Guida all'uso">
          <CircleHelp size={20} />
        </a>
      </div>
    </header>
  );
}
