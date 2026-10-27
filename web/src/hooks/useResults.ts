import { useDeferredValue, useMemo } from "react";
import { makeQuery, matches, type Query } from "../search";
import { useSession, type Tab } from "../store";
import { ALL, type Lemma, type Vocabulary } from "../vocabulary";

export type Mode = "lente" | "atlante" | "strumento";

export interface Results {
  readonly mode: Mode;
  readonly typed: Query;
  readonly search: Query | null;
  readonly searchKey: string;
  readonly inLemmas: readonly Lemma[];
  readonly inDefinitions: readonly Lemma[];
  readonly tab: Tab;
  readonly items: readonly Lemma[];
}

function pickMode(typed: Query, letter: string | null): Mode {
  if (typed.words.length > 0) return "strumento";
  return letter ? "atlante" : "lente";
}

// When the chosen tab has no results but the other one does, show the other one.
function pickTab(tab: Tab, inLemmas: number, inDefinitions: number): Tab {
  if (tab === "lemmi" && inLemmas === 0 && inDefinitions > 0) return "definizioni";
  if (tab === "definizioni" && inDefinitions === 0 && inLemmas > 0) return "lemmi";
  return tab;
}

export function useResults(vocabulary: Vocabulary): Results {
  const query = useSession((state) => state.query);
  const contained = useSession((state) => state.contained);
  const letter = useSession((state) => state.letter);
  const chosenTab = useSession((state) => state.tab);

  const typed = useMemo(() => makeQuery(query, !contained), [query, contained]);
  const mode = pickMode(typed, letter);

  // The mode follows every keystroke; the heavier filtering runs on a deferred copy
  // so typing stays responsive while 2,307 entries are scanned.
  const deferredQuery = useDeferredValue(query);
  const search = useMemo(() => {
    const deferred = makeQuery(deferredQuery, !contained);
    return deferred.words.length > 0 ? deferred : null;
  }, [deferredQuery, contained]);

  const inLemmas = useMemo(() => (search ? vocabulary.lemmas.filter((lemma) => matches(lemma.searchableLemma, search)) : []), [search, vocabulary]);
  const inDefinitions = useMemo(
    () => (search ? vocabulary.lemmas.filter((lemma) => matches(lemma.searchableDefinition, search)) : []),
    [search, vocabulary],
  );

  const tab = pickTab(chosenTab, inLemmas.length, inDefinitions.length);
  const found = tab === "lemmi" ? inLemmas : inDefinitions;
  const items = mode === "strumento" ? found : (vocabulary.byLetter.get(letter ?? ALL) ?? []);

  return {
    mode,
    typed,
    search: mode === "strumento" ? search : null,
    searchKey: `${mode}|${letter ?? ""}|${tab}|${deferredQuery}|${String(contained)}`,
    inLemmas,
    inDefinitions,
    tab,
    items,
  };
}
