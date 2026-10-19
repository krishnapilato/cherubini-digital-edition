package com.cherubinivocabulary.cherubinivocabulary.data;

import module java.base;
import module java.prefs;
import module javafx.base;

import com.cherubinivocabulary.cherubinivocabulary.model.SearchQuery;

public final class SearchHistory {
    private static final String KEY = "searchHistory";
    private static final String ENTRY_SEPARATOR = "\u001E";
    private static final int LIMIT = 30;

    private final Preferences preferences;
    private final ObservableList<SearchQuery> entries = FXCollections.observableArrayList();
    private final ObservableList<SearchQuery> readOnlyEntries = FXCollections.unmodifiableObservableList(entries);

    public SearchHistory(Preferences preferences) {
        this.preferences = Objects.requireNonNull(preferences);
        Arrays.stream(preferences.get(KEY, "").split(ENTRY_SEPARATOR))
                .map(SearchQuery::decode)
                .flatMap(Optional::stream)
                .limit(LIMIT)
                .forEach(entries::add);
    }

    public static SearchHistory forCurrentUser() {
        return new SearchHistory(Preferences.userNodeForPackage(SearchHistory.class));
    }

    public ObservableList<SearchQuery> entries() {
        return readOnlyEntries;
    }

    public void record(SearchQuery query) {
        entries.remove(query);
        entries.addFirst(query);
        if (entries.size() > LIMIT) entries.remove(LIMIT, entries.size());
        save();
    }

    public void clear() {
        entries.clear();
        save();
    }

    private void save() {
        List<String> encoded = new ArrayList<>(entries.stream().map(SearchQuery::encode).toList());
        String value = String.join(ENTRY_SEPARATOR, encoded);
        while (value.length() > Preferences.MAX_VALUE_LENGTH) {
            encoded.removeLast();
            value = String.join(ENTRY_SEPARATOR, encoded);
        }
        preferences.put(KEY, value);
    }
}
