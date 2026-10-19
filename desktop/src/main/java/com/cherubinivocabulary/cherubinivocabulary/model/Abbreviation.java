package com.cherubinivocabulary.cherubinivocabulary.model;

import module java.base;

public record Abbreviation(String category, String abbreviation, String meaning) {
    public Abbreviation {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(abbreviation, "abbreviation");
        meaning = Objects.requireNonNullElse(meaning, "");
    }
}
