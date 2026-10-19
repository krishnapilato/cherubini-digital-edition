package com.cherubinivocabulary.cherubinivocabulary.model;

import module java.base;

public record Lemma(long id, String lemma, String lemmaNo, String definizioneNo, String definition) {
    public Lemma {
        Objects.requireNonNull(lemma, "lemma");
        lemmaNo = Objects.requireNonNullElse(lemmaNo, "");
        definizioneNo = Objects.requireNonNullElse(definizioneNo, "");
        definition = Objects.requireNonNullElse(definition, "");
    }

    public String initial() {
        return TextFolding.initialOf(lemma);
    }

    public boolean lemmaStartsWith(String prefix) {
        return TextFolding.fold(lemma).startsWith(TextFolding.fold(prefix));
    }
}
