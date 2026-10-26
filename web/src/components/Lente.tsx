import { useGSAP } from "@gsap/react";
import gsap from "gsap";
import { Clock, Search } from "lucide-react";
import { useRef, type KeyboardEvent, type RefObject } from "react";
import icon from "../assets/cherubini.svg";
import { countOf, formatNumber, LETTERS, ALL } from "../vocabulary";
import { Switch } from "./Switch";

type Props = {
  query: string;
  onQuery: (query: string) => void;
  onKeyDown: (event: KeyboardEvent<HTMLInputElement>) => void;
  inputRef: RefObject<HTMLInputElement | null>;
  contained: boolean;
  onContained: (contained: boolean) => void;
  recent: string[];
  onRecent: (search: string) => void;
  onLetter: (letter: string) => void;
};

export function Lente({ query, onQuery, onKeyDown, inputRef, contained, onContained, recent, onRecent, onLetter }: Props) {
  const scope = useRef<HTMLElement>(null);

  useGSAP(
    () => {
      gsap.from(".lente-item", { y: 18, opacity: 0, duration: 0.7, stagger: 0.07, ease: "power3.out" });
      gsap.from(".pill", { scale: 0.85, opacity: 0, duration: 0.4, stagger: 0.012, delay: 0.35, ease: "back.out(2)" });
    },
    { scope },
  );

  return (
    <main className="lente" ref={scope}>
      <img className="lente-item lente-icon" src={icon} alt="" width="64" height="64" />
      <div className="lente-item lente-titles">
        <h1>Vocabolario milanese–italiano</h1>
        <p>Francesco Cherubini · voci tecniche</p>
      </div>
      <label className="lente-item big-search">
        <Search size={22} aria-hidden="true" />
        <input
          ref={inputRef}
          autoFocus
          aria-label="Cerca nel vocabolario"
          placeholder="Cerca un lemma o fino a quattro parole"
          value={query}
          onChange={(event) => onQuery(event.target.value)}
          onKeyDown={onKeyDown}
        />
        <Switch checked={contained} onChange={onContained} label="Parole contenute" />
      </label>
      <p className="lente-item hint">La ricerca parte mentre scrivi · Invio la salva tra le recenti</p>
      {recent.length > 0 && (
        <div className="lente-item recent">
          <span>Recenti</span>
          {recent.map((search) => (
            <button key={search} type="button" className="chip" onClick={() => onRecent(search)}>
              <Clock size={14} aria-hidden="true" />
              {search}
            </button>
          ))}
        </div>
      )}
      <div className="lente-item letters">
        <span>Oppure sfoglia per lettera</span>
        <div className="pills">
          {LETTERS.map((letter) => {
            const count = countOf(letter);
            const description = letter === ALL ? "Tutti i lemmi" : `${letter}: ${count ? `${formatNumber(count)} lemmi` : "nessun lemma"}`;
            return (
              <button key={letter} type="button" className="pill" disabled={count === 0} title={description} aria-label={description} onClick={() => onLetter(letter)}>
                {letter}
              </button>
            );
          })}
        </div>
      </div>
    </main>
  );
}
