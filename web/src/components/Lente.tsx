import { useGSAP } from "@gsap/react";
import gsap from "gsap";
import { Clock, Search, X } from "lucide-react";
import { useRef, type KeyboardEvent, type RefObject } from "react";
import { ENTER, EXIT } from "../motion";
import { MAX_QUERY_LENGTH } from "../search";
import { useRecent, useSession } from "../store";
import { describeLetter, LETTERS } from "../vocabulary";
import { Switch } from "./Switch";

interface Props {
  readonly onKeyDown: (event: KeyboardEvent<HTMLInputElement>) => void;
  readonly inputRef: RefObject<HTMLInputElement | null>;
  readonly countOf: (letter: string) => number;
}

export function Lente({ onKeyDown, inputRef, countOf }: Props) {
  const scope = useRef<HTMLElement>(null);
  const query = useSession((state) => state.query);
  const contained = useSession((state) => state.contained);
  const recent = useRecent((state) => state.recent);
  const { setQuery, setContained, openLetter } = useSession.getState();
  const { remember, forget } = useRecent.getState();

  const { contextSafe } = useGSAP(
    () => {
      gsap.from(".lente-item", { y: 12, opacity: 0, stagger: 0.07, ...ENTER });
      gsap.from(".pill", {
        scale: 0.94,
        opacity: 0,
        stagger: 0.012,
        delay: 0.3,
        ...ENTER,
      });
    },
    { scope },
  );

  const vanish = contextSafe((chip: HTMLElement | null, search: string) => {
    if (chip) gsap.to(chip, { scale: 0.8, opacity: 0, ...EXIT, onComplete: () => forget(search) });
    else forget(search);
  });

  return (
    <main className="lente" ref={scope}>
      <img
        className="lente-item lente-icon"
        src={`${import.meta.env.BASE_URL}favicon.svg`}
        alt=""
        width="64"
        height="64"
      />
      <div className="lente-item lente-titles">
        <h1>Vocabolario milanese–italiano</h1>
        <p>Francesco Cherubini · voci tecniche</p>
      </div>
      <search className="lente-item big-search">
        <Search size={22} aria-hidden="true" />
        <input
          ref={inputRef}
          type="search"
          aria-label="Cerca nel vocabolario"
          placeholder="Cerca una parola"
          maxLength={MAX_QUERY_LENGTH}
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          onKeyDown={onKeyDown}
        />
        <Switch checked={contained} onChange={setContained} label="Parole contenute" />
      </search>
      <p className="lente-item hint">Invio salva la ricerca tra le recenti</p>
      {recent.length > 0 && (
        <fieldset className="lente-item recent" aria-label="Ricerche recenti">
          <span aria-hidden="true">Recenti</span>
          {recent.map((search) => (
            <span key={search} className="chip">
              <button
                type="button"
                className="chip-search"
                onClick={() => {
                  setQuery(search);
                  remember(search);
                }}
              >
                <Clock size={14} aria-hidden="true" />
                {search}
              </button>
              <button
                type="button"
                className="chip-remove"
                aria-label={`Togli «${search}» dalle recenti`}
                title="Togli dalle recenti"
                onClick={(event) => vanish(event.currentTarget.parentElement, search)}
              >
                <X size={13} aria-hidden="true" />
              </button>
            </span>
          ))}
        </fieldset>
      )}
      <nav className="lente-item letters" aria-label="Sfoglia per lettera">
        <span aria-hidden="true">Oppure sfoglia per lettera</span>
        <div className="pills">
          {LETTERS.map((letter) => {
            const count = countOf(letter);
            const description = describeLetter(letter, count);
            return (
              <button
                key={letter}
                type="button"
                className="pill"
                disabled={count === 0}
                title={description}
                aria-label={description}
                onClick={() => openLetter(letter)}
              >
                {letter}
              </button>
            );
          })}
        </div>
      </nav>
    </main>
  );
}
