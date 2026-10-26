import rows from "../../data/cherubini.json";
import { fold } from "./search";

export type Lemma = {
  id: number;
  lemma: string;
  definition: string;
  searchableLemma: string;
  searchableDefinition: string;
  initial: string;
};

type Row = { id: number; lemma: string; def2: string | null };

export const ALL = "*";

export const LETTERS = [ALL, ..."ABCDEFGHIJKLMNOPQRSTUVWXYZ"];

export const lemmas: Lemma[] = (rows as Row[]).map((row) => {
  const definition = (row.def2 ?? "").trim();
  const searchableLemma = fold(row.lemma);
  return {
    id: row.id,
    lemma: row.lemma,
    definition,
    searchableLemma,
    searchableDefinition: fold(definition),
    initial: (searchableLemma.match(/\p{L}/u)?.[0] ?? "").toUpperCase(),
  };
});

export function countOf(letter: string): number {
  return letter === ALL ? lemmas.length : lemmas.filter((lemma) => lemma.initial === letter).length;
}

export function lemmasOf(letter: string): Lemma[] {
  return letter === ALL ? lemmas : lemmas.filter((lemma) => lemma.initial === letter);
}

export function formatNumber(value: number): string {
  return value.toLocaleString("it-IT");
}
