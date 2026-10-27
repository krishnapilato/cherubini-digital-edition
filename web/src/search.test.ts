import { describe as group, expect, it } from "vitest";
import data from "../../data/cherubini.json?raw";
import cases from "../../tests/search-cases.json";
import { describe, find, fold, makeQuery, matches } from "./search";
import { ALL, build } from "./vocabulary";

const vocabulary = build(data);

group("fold", () => {
  it.each(cases.fold)("$text → $folded", ({ text, folded }) => {
    expect(fold(text)).toBe(folded);
    expect(fold(text)).toHaveLength(text.length);
  });
});

group("makeQuery", () => {
  it.each(cases.queries)("«$text»", ({ text, exact, words, ignored, description }) => {
    const query = makeQuery(text, exact);
    expect(query.words).toEqual(words);
    expect(query.ignored).toEqual(ignored);
    expect(describe(query)).toBe(description);
  });
});

group("find", () => {
  it.each(cases.highlights)("«$query» in «$text» (exact: $exact)", ({ text, query, exact, ranges }) => {
    const found = find(fold(text), makeQuery(query, exact));
    expect(found.map(({ start, end }) => [start, end])).toEqual(ranges);
  });
});

group("search in the vocabulary", () => {
  it.each(cases.searches)("«$text» (contained: $contained)", ({ text, contained, lemmas, definitions }) => {
    const query = makeQuery(text, !contained);
    const ids = (field: "searchableLemma" | "searchableDefinition") =>
      vocabulary.lemmas.filter((lemma) => matches(lemma[field], query)).map((lemma) => lemma.id);
    expect(ids("searchableLemma")).toEqual(lemmas);
    expect(ids("searchableDefinition")).toEqual(definitions);
  });
});

group("vocabulary", () => {
  it("contains every entry, in order", () => {
    const ids = vocabulary.lemmas.map((lemma) => lemma.id);
    expect(ids).toHaveLength(2307);
    expect(ids).toEqual(ids.toSorted((a, b) => a - b));
  });

  it("files the entries under their initial letter", () => {
    const counts = Object.fromEntries([...vocabulary.byLetter].filter(([letter]) => letter !== ALL).map(([letter, group]) => [letter, group.length]));
    expect(counts).toEqual(cases.letters);
  });
});
