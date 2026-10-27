import { Search, X } from "lucide-react";
import type { KeyboardEvent, ReactNode, RefObject } from "react";
import icon from "../assets/cherubini.svg";
import { useSession } from "../store";
import { Switch } from "./Switch";

interface Props {
  readonly onKeyDown: (event: KeyboardEvent<HTMLInputElement>) => void;
  readonly inputRef: RefObject<HTMLInputElement | null>;
  readonly onHome: () => void;
  readonly children: ReactNode;
}

export function TopBar({ onKeyDown, inputRef, onHome, children }: Props) {
  const query = useSession((state) => state.query);
  const contained = useSession((state) => state.contained);
  const { setQuery, setContained } = useSession.getState();

  return (
    <header className="topbar">
      <button type="button" className="home" onClick={onHome} aria-label="Pagina iniziale" title="Pagina iniziale">
        <img src={icon} alt="" width="30" height="30" />
        <span aria-hidden="true">Cherubini</span>
      </button>
      <search className="bar-search">
        <Search size={17} aria-hidden="true" />
        <input
          ref={inputRef}
          type="search"
          aria-label="Cerca nel vocabolario"
          placeholder="Cerca un lemma o fino a quattro parole"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          onKeyDown={onKeyDown}
        />
        {query && (
          <button type="button" className="icon-button small" aria-label="Azzera la ricerca" title="Azzera (Esc)" onClick={() => setQuery("")}>
            <X size={15} aria-hidden="true" />
          </button>
        )}
      </search>
      <Switch checked={contained} onChange={setContained} label="Parole contenute" />
      <div className="topbar-actions">{children}</div>
    </header>
  );
}
