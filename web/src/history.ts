const KEY = "cherubini.recentSearches";
const LIMIT = 6;

export function loadRecent(): string[] {
  try {
    const saved = JSON.parse(sessionStorage.getItem(KEY) ?? "[]");
    return Array.isArray(saved) ? saved.filter((item) => typeof item === "string").slice(0, LIMIT) : [];
  } catch {
    return [];
  }
}

export function rememberSearch(search: string, recent: string[]): string[] {
  const cleaned = search.trim();
  if (!cleaned) return recent;
  return save([cleaned, ...recent.filter((item) => item !== cleaned)].slice(0, LIMIT));
}

export function forgetSearch(search: string, recent: string[]): string[] {
  return save(recent.filter((item) => item !== search));
}

function save(recent: string[]): string[] {
  try {
    sessionStorage.setItem(KEY, JSON.stringify(recent));
  } catch {
    return recent;
  }
  return recent;
}
