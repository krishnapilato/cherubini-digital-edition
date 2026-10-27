import { useSession } from "../store";
import { ALL, formatNumber, LETTERS } from "../vocabulary";

export function LetterGrid({ countOf }: Readonly<{ countOf: (letter: string) => number }>) {
  const letter = useSession((state) => state.letter) ?? ALL;
  const openLetter = useSession((state) => state.openLetter);

  return (
    <nav className="card letters-card" aria-label="Lettere">
      <h2 className="card-label">Sfoglia per lettera</h2>
      <div className="tiles">
        {LETTERS.map((key) => {
          const count = countOf(key);
          const description = key === ALL ? "Tutti i lemmi" : `${key}: ${formatNumber(count)} lemmi`;
          return (
            <button
              key={key}
              type="button"
              className={key === letter ? "tile selected" : "tile"}
              aria-current={key === letter ? "page" : undefined}
              aria-label={description}
              title={description}
              disabled={count === 0}
              onClick={() => openLetter(key)}
            >
              <span className="tile-letter" aria-hidden="true">
                {key}
              </span>
              <span className="tile-count" aria-hidden="true">
                {formatNumber(count)}
              </span>
            </button>
          );
        })}
      </div>
    </nav>
  );
}
