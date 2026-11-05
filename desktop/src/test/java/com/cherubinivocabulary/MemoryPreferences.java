package com.cherubinivocabulary;

import module java.base;
import module java.prefs;

/** Keeps the tests away from the real preferences of whoever runs them. */
final class MemoryPreferences extends AbstractPreferences {
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
