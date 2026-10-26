const KEY = "cherubini.recentSearches";
const LIMIT = 6;

export function loadRecent(): string[] {
  try {
    const saved = JSON.parse(localStorage.getItem(KEY) ?? "[]");
    return Array.isArray(saved) ? saved.filter((item) => typeof item === "string").slice(0, LIMIT) : [];
  } catch {
    return [];
  }
}

export function rememberSearch(search: string, recent: string[]): string[] {
  const cleaned = search.trim();
  if (!cleaned) return recent;
  const updated = [cleaned, ...recent.filter((item) => item !== cleaned)].slice(0, LIMIT);
  try {
    localStorage.setItem(KEY, JSON.stringify(updated));
  } catch {
    return updated;
  }
  return updated;
}
