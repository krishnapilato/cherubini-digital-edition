package com.cherubinivocabulary;

public record Lemma(long id, String lemma, String definition, String searchableLemma, String searchableDefinition) {

    static Lemma of(long id, String lemma, String definition) {
        var text = definition == null ? "" : definition.strip();
        return new Lemma(id, lemma, text, Search.fold(lemma), Search.fold(text));
    }

    String initial() {
        return searchableLemma.chars()
                .filter(Character::isLetter)
                .map(Character::toUpperCase)
                .mapToObj(Character::toString)
                .findFirst()
                .orElse("");
    }
}
