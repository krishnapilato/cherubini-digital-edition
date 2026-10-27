import { create } from "zustand";
import { createJSONStorage, persist } from "zustand/middleware";

export type Tab = "lemmi" | "definizioni";

const RECENT_LIMIT = 6;

interface Session {
  readonly query: string;
  readonly contained: boolean;
  readonly letter: string | null;
  readonly tab: Tab;
  readonly selectedId: number | null;
  readonly toPrint: readonly number[];
  readonly recent: readonly string[];
}

interface Actions {
  setQuery: (query: string) => void;
  setContained: (contained: boolean) => void;
  openLetter: (letter: string) => void;
  goHome: () => void;
  setTab: (tab: Tab) => void;
  select: (id: number) => void;
  togglePrint: (id: number) => void;
  addToPrint: (ids: readonly number[]) => void;
  clearPrint: () => void;
  remember: (search: string) => void;
  forget: (search: string) => void;
}

const initial: Session = {
  query: "",
  contained: false,
  letter: null,
  tab: "definizioni",
  selectedId: null,
  toPrint: [],
  recent: [],
};

// Everything the reader is doing lives in sessionStorage: a reload keeps the same view,
// closing the tab forgets it.
export const useSession = create<Session & Actions>()(
  persist(
    (set) => ({
      ...initial,
      setQuery: (query) => set({ query }),
      setContained: (contained) => set({ contained }),
      openLetter: (letter) => set({ letter, query: "", selectedId: null }),
      goHome: () => set({ letter: null, query: "" }),
      setTab: (tab) => set({ tab }),
      select: (selectedId) => set({ selectedId }),
      togglePrint: (id) =>
        set(({ toPrint }) => ({ toPrint: toPrint.includes(id) ? toPrint.filter((item) => item !== id) : [...toPrint, id] })),
      addToPrint: (ids) => set(({ toPrint }) => ({ toPrint: [...new Set([...toPrint, ...ids])] })),
      clearPrint: () => set({ toPrint: [] }),
      remember: (search) => {
        const cleaned = search.trim();
        if (cleaned) set(({ recent }) => ({ recent: [cleaned, ...recent.filter((item) => item !== cleaned)].slice(0, RECENT_LIMIT) }));
      },
      forget: (search) => set(({ recent }) => ({ recent: recent.filter((item) => item !== search) })),
    }),
    {
      name: "cherubini.session",
      version: 1,
      storage: createJSONStorage(() => sessionStorage),
      partialize: ({ query, contained, letter, tab, selectedId, toPrint, recent }): Session => ({
        query,
        contained,
        letter,
        tab,
        selectedId,
        toPrint,
        recent,
      }),
    },
  ),
);
