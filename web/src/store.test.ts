import { beforeEach, describe, expect, it } from "vitest";
import { useSession } from "./store";

const session = () => useSession.getState();

describe("recent searches", () => {
  beforeEach(() => {
    sessionStorage.clear();
    useSession.setState({ recent: [] });
  });

  it("puts the latest search first, trimmed and without duplicates", () => {
    session().remember("ferro");
    session().remember("legno");
    session().remember("  ferro ");
    expect(session().recent).toEqual(["ferro", "legno"]);
  });

  it("ignores empty searches", () => {
    session().remember("   ");
    expect(session().recent).toEqual([]);
  });

  it("keeps the six most recent", () => {
    for (const search of ["a", "b", "c", "d", "e", "f", "g"]) session().remember(search);
    expect(session().recent).toEqual(["g", "f", "e", "d", "c", "b"]);
  });

  it("forgets a single search", () => {
    session().remember("ferro");
    session().remember("legno");
    session().forget("ferro");
    expect(session().recent).toEqual(["legno"]);
  });

  it("saves the session in sessionStorage", () => {
    session().remember("ferro");
    expect(sessionStorage.getItem("cherubini.session")).toContain('"recent":["ferro"]');
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
