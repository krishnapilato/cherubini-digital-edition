import { describe } from "../search";
import { useSession } from "../store";
import type { Results } from "../hooks/useResults";
import { ALL, formatNumber, quantity } from "../vocabulary";

const TABS = [
  { key: "lemmi", label: "Lemmi" },
  { key: "definizioni", label: "Definizioni" },
] as const;

export function ListHeader({ results }: Readonly<{ results: Results }>) {
  const letter = useSession((state) => state.letter);
  const setTab = useSession((state) => state.setTab);

  if (results.mode !== "strumento") {
    return (
      <div className="list-header atlante-header">
        <h2 className="list-title">{letter === ALL ? "Tutti i lemmi" : letter}</h2>
        <span className="list-count">{quantity(results.items.length, "lemma", "lemmi")}</span>
      </div>
    );
  }

  return (
    <div className="list-header">
      <div className="segmented" role="group" aria-label="Dove cercare">
        {TABS.map(({ key, label }) => {
          const count = key === "lemmi" ? results.inLemmas.length : results.inDefinitions.length;
          const selected = results.tab === key;
          return (
            <button key={key} type="button" aria-pressed={selected} className={selected ? "selected" : ""} disabled={count === 0} onClick={() => setTab(key)}>
              {label}
              <span className="count">{formatNumber(count)}</span>
            </button>
          );
        })}
      </div>
      {results.search && <p className="summary">{describe(results.search)}</p>}
    </div>
  );
}
