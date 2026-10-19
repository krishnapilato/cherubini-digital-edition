package com.cherubinivocabulary.cherubinivocabulary.model;

import module java.base;

public record SearchResult(List<Lemma> inLemmas, List<Lemma> inDefinitions) {
    public SearchResult {
        inLemmas = List.copyOf(inLemmas);
        inDefinitions = List.copyOf(inDefinitions);
    }

    public boolean isEmpty() {
        return inLemmas.isEmpty() && inDefinitions.isEmpty();
    }
}
