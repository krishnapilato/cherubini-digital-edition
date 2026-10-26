import { ALL, countOf, formatNumber, LETTERS } from "../vocabulary";

export function LetterGrid({ letter, onLetter }: { letter: string; onLetter: (letter: string) => void }) {
  return (
    <section className="card letters-card" aria-label="Lettere">
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
              aria-pressed={key === letter}
              aria-label={description}
              title={description}
              disabled={count === 0}
              onClick={() => onLetter(key)}
            >
              <span className="tile-letter">{key}</span>
              <span className="tile-count">{formatNumber(count)}</span>
            </button>
          );
        })}
      </div>
    </section>
  );
}
