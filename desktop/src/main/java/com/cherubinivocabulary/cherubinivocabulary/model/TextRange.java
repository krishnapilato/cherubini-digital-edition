package com.cherubinivocabulary.cherubinivocabulary.model;

import module java.base;

public record TextRange(int start, int end) {
    public TextRange {
        if (start < 0 || end < start) throw new IllegalArgumentException("Invalid range [%d, %d)".formatted(start, end));
    }

    public boolean overlapsOrTouches(TextRange other) {
        return other.start <= end && start <= other.end;
    }

    public TextRange union(TextRange other) {
        return new TextRange(Math.min(start, other.start), Math.max(end, other.end));
    }
}
