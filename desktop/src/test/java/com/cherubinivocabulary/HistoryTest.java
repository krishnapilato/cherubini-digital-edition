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

    /** Keeps the tests away from the real preferences of whoever runs them. */
    private static final class MemoryPreferences extends AbstractPreferences {
        private final Map<String, String> values = new HashMap<>();
        private final Map<String, MemoryPreferences> children = new HashMap<>();

        MemoryPreferences() {
            this(null, "");
        }

        private MemoryPreferences(MemoryPreferences parent, String name) {
            super(parent, name);
        }

        @Override
        protected void putSpi(String key, String value) {
            values.put(key, value);
        }

        @Override
        protected String getSpi(String key) {
            return values.get(key);
        }

        @Override
        protected void removeSpi(String key) {
            values.remove(key);
        }

        @Override
        protected void removeNodeSpi() {
            values.clear();
        }

        @Override
        protected String[] keysSpi() {
            return values.keySet().toArray(String[]::new);
        }

        @Override
        protected String[] childrenNamesSpi() {
            return children.keySet().toArray(String[]::new);
        }

        @Override
        protected AbstractPreferences childSpi(String name) {
            return children.computeIfAbsent(name, child -> new MemoryPreferences(this, child));
        }

        @Override
        protected void syncSpi() {
            // Nothing to read back: the values live only in memory.
        }

        @Override
        protected void flushSpi() {
            // Nothing to write: the values live only in memory.
        }
    }
}
