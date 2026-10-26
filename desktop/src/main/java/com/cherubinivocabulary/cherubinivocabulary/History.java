package com.cherubinivocabulary.cherubinivocabulary;

import module java.base;
import module java.prefs;

final class History {
    private static final String KEY = "recentSearches";
    private static final String SEPARATOR = "\u001E";
    private static final int LIMIT = 6;

    private final Preferences preferences = Preferences.userNodeForPackage(History.class);

    List<String> recent() {
        String saved = preferences.get(KEY, "");
        return saved.isEmpty() ? List.of() : List.of(saved.split(SEPARATOR));
    }

    void remember(String search) {
        String cleaned = search.strip();
        if (cleaned.isEmpty()) return;
        List<String> updated = new ArrayList<>(List.of(cleaned));
        recent().stream().filter(previous -> !previous.equals(cleaned)).limit(LIMIT - 1).forEach(updated::add);
        preferences.put(KEY, String.join(SEPARATOR, updated));
    }
}
