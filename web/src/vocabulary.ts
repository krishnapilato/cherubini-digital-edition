import dataUrl from "../../data/cherubini.json?url";
import { fold } from "./search";

export interface Lemma {
  readonly id: number;
  readonly lemma: string;
  readonly definition: string;
  readonly searchableLemma: string;
  readonly searchableDefinition: string;
  readonly initial: string;
}

export interface Vocabulary {
  readonly lemmas: readonly Lemma[];
  readonly byLetter: ReadonlyMap<string, readonly Lemma[]>;
}

interface Row {
  readonly id: number;
  readonly lemma: string;
  readonly def2: string | null;
}

export const ALL = "*";
export const LETTERS: readonly string[] = [ALL, ...Array.from({ length: 26 }, (_, index) => String.fromCodePoint(65 + index))];

const CACHE_KEY = "cherubini.vocabulary";
const numbers = new Intl.NumberFormat("it-IT");

export const formatNumber = (value: number): string => numbers.format(value);

export const quantity = (count: number, one: string, many: string): string => `${formatNumber(count)} ${count === 1 ? one : many}`;

function cached(): string | null {
  try {
    return sessionStorage.getItem(CACHE_KEY);
  } catch {
    return null;
  }
}

function store(text: string): boolean {
  try {
    sessionStorage.setItem(CACHE_KEY, text);
    return true;
  } catch {
    return false;
  }
}

async function download(): Promise<string> {
  const response = await fetch(dataUrl);
  if (!response.ok) throw new Error(`Vocabolario non disponibile (${response.status})`);
  const text = await response.text();
  store(text);
  return text;
}

function toLemma({ id, lemma, def2 }: Row): Lemma {
  const definition = (def2 ?? "").trim();
  const searchableLemma = fold(lemma);
  return {
    id,
    lemma,
    definition,
    searchableLemma,
    searchableDefinition: fold(definition),
    initial: (/\p{L}/u.exec(searchableLemma)?.[0] ?? "").toUpperCase(),
  };
}

export function build(text: string): Vocabulary {
  const lemmas = (JSON.parse(text) as Row[]).map(toLemma);
  const byLetter = new Map<string, Lemma[]>([[ALL, lemmas]]);
  for (const lemma of lemmas) {
    const group = byLetter.get(lemma.initial);
    if (group) group.push(lemma);
    else byLetter.set(lemma.initial, [lemma]);
  }
  return { lemmas, byLetter };
}

// The vocabulary is cached in sessionStorage: it survives reloads of the tab and is
// discarded by the browser when the tab is closed.
export const vocabularyReady: Promise<Vocabulary | null> = Promise.resolve(cached() ?? download())
  .then(build)
  .catch(() => null);
