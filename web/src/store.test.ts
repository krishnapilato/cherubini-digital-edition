import { beforeEach, describe, expect, it, vi } from "vitest";
import { useRecent, useSession } from "./store";

// Node offers localStorage only with a backing file: the store gets an in-memory one,
// in place before the store module reads it.
vi.hoisted(() => {
  const items = new Map<string, string>();
  const memory: Storage = {
    get length() {
      return items.size;
    },
    clear: () => items.clear(),
    getItem: (key) => items.get(key) ?? null,
    key: (index) => [...items.keys()][index] ?? null,
    removeItem: (key) => items.delete(key),
    setItem: (key, value) => items.set(key, value),
  };
  Object.defineProperty(globalThis, "localStorage", { value: memory, configurable: true });
});

const session = () => useSession.getState();
const recents = () => useRecent.getState();

describe("recent searches", () => {
  beforeEach(() => {
    localStorage.clear();
    useRecent.setState({ recent: [] });
  });

  it("puts the latest search first, trimmed and without duplicates", () => {
    recents().remember("ferro");
    recents().remember("legno");
    recents().remember("  ferro ");
    expect(recents().recent).toEqual(["ferro", "legno"]);
  });

  it("ignores empty searches", () => {
    recents().remember("   ");
    expect(recents().recent).toEqual([]);
  });

  it("keeps the six most recent", () => {
    for (const search of ["a", "b", "c", "d", "e", "f", "g"]) recents().remember(search);
    expect(recents().recent).toEqual(["g", "f", "e", "d", "c", "b"]);
  });

  it("forgets a single search", () => {
    recents().remember("ferro");
    recents().remember("legno");
    recents().forget("ferro");
    expect(recents().recent).toEqual(["legno"]);
  });

  it("keeps them in localStorage, apart from the session", () => {
    recents().remember("ferro");
    expect(localStorage.getItem("cherubini.recent")).toContain('"recent":["ferro"]');
    expect(sessionStorage.getItem("cherubini.session") ?? "").not.toContain("ferro");
  });
});

describe("print selection", () => {
  beforeEach(() => useSession.setState({ toPrint: [] }));

  it("toggles an entry", () => {
    session().togglePrint(1);
    session().togglePrint(2);
    session().togglePrint(1);
    expect(session().toPrint).toEqual([2]);
  });

  it("adds many entries without duplicates", () => {
    session().togglePrint(1);
    session().addToPrint([1, 2, 3]);
    expect(session().toPrint).toEqual([1, 2, 3]);
  });
});

describe("selection", () => {
  beforeEach(() => useSession.setState({ selectedId: 42, tab: "definizioni" }));

  it("starts a new search from its first entry", () => {
    session().setQuery("ll");
    expect(session().selectedId).toBeNull();
  });

  it("starts from the first entry when contained words are switched", () => {
    session().setContained(false);
    expect(session().selectedId).toBeNull();
  });

  it("keeps the open entry when switching tab", () => {
    session().setTab("lemmi");
    expect(session().selectedId).toBe(42);
  });
});
