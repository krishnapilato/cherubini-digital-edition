package com.cherubinivocabulary;

import module java.base;
import module java.prefs;

public final class History {
    private static final String KEY = "recentSearches";

    private final Preferences preferences;

    public History() {
        this(Preferences.userNodeForPackage(History.class));
    }

    History(Preferences preferences) {
        this.preferences = preferences;
    }

    List<String> recent() {
        var saved = preferences.get(KEY, "");
        return saved.isEmpty() ? List.of() : List.of(saved.split("\u001E"));
    }

    void remember(String search) {
        var cleaned = search.strip();
        if (!cleaned.isEmpty()) save(Stream.concat(Stream.of(cleaned), recent().stream().filter(s -> !s.equals(cleaned))).limit(6).toList());
    }

    void forget(String search) {
        save(recent().stream().filter(s -> !s.equals(search)).toList());
    }

    private void save(List<String> searches) {
        if (searches.isEmpty()) preferences.remove(KEY);
        else preferences.put(KEY, String.join("\u001E", searches));
    }
}
