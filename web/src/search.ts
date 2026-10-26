export const MAX_WORDS = 4;

export type Range = { start: number; end: number };

export type Query = { words: string[]; ignored: string[]; exact: boolean };

const WORD_CHARACTER = /[\p{L}\p{N}]/u;
const COMBINING_MARK = /\p{Mn}/u;

export function fold(text: string): string {
  let folded = "";
  for (let i = 0; i < text.length; i++) {
    const base = text[i].normalize("NFD")[0];
    folded += COMBINING_MARK.test(base) ? " " : base.toLowerCase()[0];
  }
  return folded;
}

export function makeQuery(text: string, exact: boolean): Query {
  const all = text.trim().split(/\s+/).filter(Boolean);
  return { words: all.slice(0, MAX_WORDS), ignored: all.slice(MAX_WORDS), exact };
}

export function describe(query: Query): string {
  const words = query.words.map((word) => `«${word}»`).join(" e ");
  return `${words} · ${query.exact ? "parole esatte" : "anche parole contenute"}`;
}

export function matches(searchable: string, query: Query): boolean {
  return query.words.every((word) => findWords(searchable, [word], query.exact).length > 0);
}

export function find(searchable: string, query: Query): Range[] {
  return findWords(searchable, query.words, query.exact);
}

function findWords(text: string, words: string[], exact: boolean): Range[] {
  const found: Range[] = [];
  for (const word of words) {
    const term = fold(word);
    if (!term.trim()) continue;
    for (let at = text.indexOf(term); at >= 0; at = text.indexOf(term, at + 1)) {
      if (!exact || isWholeWord(text, at, at + term.length)) found.push({ start: at, end: at + term.length });
    }
  }
  return merge(found);
}

function isWholeWord(text: string, start: number, end: number): boolean {
  const before = start === 0 || !WORD_CHARACTER.test(text[start - 1]);
  const after = end === text.length || !WORD_CHARACTER.test(text[end]);
  return before && after;
}

function merge(ranges: Range[]): Range[] {
  const merged: Range[] = [];
  for (const range of [...ranges].sort((a, b) => a.start - b.start)) {
    const last = merged.at(-1);
    if (last && range.start <= last.end) last.end = Math.max(last.end, range.end);
    else merged.push({ ...range });
  }
  return merged;
}
