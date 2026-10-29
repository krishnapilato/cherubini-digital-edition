package com.cherubinivocabulary;

import module java.base;
import module java.prefs;

public final class History {
    private static final String KEY = "recentSearches";
    private static final String SEP = "\u001E";
    private static final int LIMIT = 6;

    private final Preferences preferences;

    public History() {
        this(Preferences.userNodeForPackage(History.class));
    }

    History(Preferences preferences) {
        this.preferences = preferences;
    }

    List<String> recent() {
        var saved = preferences.get(KEY, "");
        return saved.isEmpty() ? List.of() : List.of(saved.split(SEP));
    }

    void remember(String search) {
        var cleaned = search.strip();
        if (!cleaned.isEmpty()) save(Stream.concat(Stream.of(cleaned), recent().stream().filter(s -> !s.equals(cleaned))).limit(LIMIT).toList());
    }

    void forget(String search) {
        save(recent().stream().filter(s -> !s.equals(search)).toList());
    }

    private void save(List<String> searches) {
        if (searches.isEmpty()) preferences.remove(KEY);
        else preferences.put(KEY, String.join(SEP, searches));
    }
}
