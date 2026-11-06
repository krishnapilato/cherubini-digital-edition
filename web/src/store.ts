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
}

const initial: Session = {
  query: "",
  contained: true,
  letter: null,
  tab: "definizioni",
  selectedId: null,
  toPrint: [],
};

// What the reader is doing lives in sessionStorage: a reload keeps the same view,
// closing the tab forgets it. Recent searches are kept apart, see useRecent.
export const useSession = create<Session & Actions>()(
  persist(
    (set) => ({
      ...initial,
      // A new search starts from its first entry, as on the desktop; switching tab keeps the open one.
      setQuery: (query) => set({ query, selectedId: null }),
      setContained: (contained) => set({ contained, selectedId: null }),
      openLetter: (letter) => set({ letter, query: "", selectedId: null }),
      goHome: () => set({ letter: null, query: "" }),
      setTab: (tab) => set({ tab }),
      select: (selectedId) => set({ selectedId }),
      togglePrint: (id) =>
        set(({ toPrint }) => ({
          toPrint: toPrint.includes(id) ? toPrint.filter((item) => item !== id) : [...toPrint, id],
        })),
      addToPrint: (ids) => set(({ toPrint }) => ({ toPrint: [...new Set([...toPrint, ...ids])] })),
      clearPrint: () => set({ toPrint: [] }),
    }),
    {
      name: "cherubini.session",
      version: 1,
      storage: createJSONStorage(() => sessionStorage),
      partialize: ({ query, contained, letter, tab, selectedId, toPrint }): Session => ({
        query,
        contained,
        letter,
        tab,
        selectedId,
        toPrint,
      }),
    },
  ),
);

interface Recent {
  readonly recent: readonly string[];
  readonly remember: (search: string) => void;
  readonly forget: (search: string) => void;
}

export const RECENT_KEY = "cherubini.recent";

// Recent searches are worth finding again at the next visit, as on the desktop, so they
// live in localStorage; they never leave the browser.
export const useRecent = create<Recent>()(
  persist(
    (set) => ({
      recent: [],
      remember: (search) => {
        const cleaned = search.trim();
        if (cleaned)
          set(({ recent }) => ({
            recent: [cleaned, ...recent.filter((item) => item !== cleaned)].slice(0, RECENT_LIMIT),
          }));
      },
      forget: (search) => set(({ recent }) => ({ recent: recent.filter((item) => item !== search) })),
    }),
    {
      name: RECENT_KEY,
      version: 1,
      storage: createJSONStorage(() => localStorage),
      partialize: ({ recent }) => ({ recent }),
    },
  ),
);
