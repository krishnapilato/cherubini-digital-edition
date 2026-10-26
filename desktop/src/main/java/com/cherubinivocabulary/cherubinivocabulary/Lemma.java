package com.cherubinivocabulary.cherubinivocabulary;

import module java.base;

record Lemma(long id, String lemma, String definition, String searchableLemma, String searchableDefinition) {

    static Lemma of(long id, String lemma, String definition) {
        String text = definition == null ? "" : definition.strip();
        return new Lemma(id, lemma, text, Search.fold(lemma), Search.fold(text));
    }

    String initial() {
        return searchableLemma.chars()
                .filter(Character::isLetter)
                .mapToObj(letter -> Character.toString(letter).toUpperCase(Locale.ITALIAN))
                .findFirst()
                .orElse("");
    }
}