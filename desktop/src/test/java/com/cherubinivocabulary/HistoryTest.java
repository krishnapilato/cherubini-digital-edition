package com.cherubinivocabulary;

import module java.base;
import module java.prefs;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HistoryTest {
    private static final String FERRO = "ferro";
    private static final String LEGNO = "legno";

    private final Preferences preferences = new MemoryPreferences();
    private final History history = new History(preferences);

    @Test
    void putsTheLatestSearchFirstTrimmedAndWithoutDuplicates() {
        history.remember(FERRO);
        history.remember(LEGNO);
        history.remember("  " + FERRO + " ");
        assertEquals(List.of(FERRO, LEGNO), history.recent());
    }

    @Test
    void ignoresEmptySearches() {
        history.remember("   ");
        assertEquals(List.of(), history.recent());
    }

    @Test
    void keepsTheSixMostRecent() {
        List.of("a", "b", "c", "d", "e", "f", "g").forEach(history::remember);
        assertEquals(List.of("g", "f", "e", "d", "c", "b"), history.recent());
    }

    @Test
    void forgetsASingleSearchAndClearsTheKeyWhenEmpty() {
        history.remember(FERRO);
        history.remember(LEGNO);
        history.forget(FERRO);
        assertEquals(List.of(LEGNO), history.recent());
        history.forget(LEGNO);
        assertNull(preferences.get("recentSearches", null));
    }
}
