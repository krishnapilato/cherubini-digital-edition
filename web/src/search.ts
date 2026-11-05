export const MAX_WORDS = 4;

export interface Range {
  readonly start: number;
  readonly end: number;
}

export interface Query {
  readonly words: readonly string[];
  readonly ignored: readonly string[];
  readonly exact: boolean;
}

const WORD_CHARACTER = /[\p{L}\p{N}]/u;
const COMBINING_MARK = /\p{Mn}/u;

// Lowercases and strips accents while keeping the exact length of the text, so the
// positions found in the folded copy can highlight the original one.
export function fold(text: string): string {
  let folded = "";
  for (let i = 0; i < text.length; i++) {
    const base = text.charAt(i).normalize("NFD").charAt(0);
    folded += COMBINING_MARK.test(base) ? " " : base.toLowerCase().charAt(0);
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

function findWords(text: string, words: readonly string[], exact: boolean): Range[] {
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
  const before = start === 0 || !WORD_CHARACTER.test(text.charAt(start - 1));
  const after = end === text.length || !WORD_CHARACTER.test(text.charAt(end));
  return before && after;
}

function merge(ranges: readonly Range[]): Range[] {
  const merged: { start: number; end: number }[] = [];
  for (const range of [...ranges].sort((a, b) => a.start - b.start)) {
    const last = merged.at(-1);
    if (last && range.start <= last.end) last.end = Math.max(last.end, range.end);
    else merged.push({ ...range });
  }
  return merged;
}
