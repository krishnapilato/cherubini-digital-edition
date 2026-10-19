package com.cherubinivocabulary.cherubinivocabulary.model;

import module java.base;

public record SearchQuery(List<String> terms, MatchMode mode) {
    public static final int MAX_TERMS = 4;

    private static final String SEPARATOR = "\u001F";

    public SearchQuery {
        Objects.requireNonNull(mode, "mode");
        terms = terms.stream()
                .map(String::strip)
                .filter(term -> !term.isEmpty())
                .limit(MAX_TERMS)
                .toList();
        if (terms.isEmpty()) throw new IllegalArgumentException("A search needs at least one term.");
    }

    public static Optional<SearchQuery> of(List<String> rawTerms, MatchMode mode) {
        return rawTerms.stream().allMatch(String::isBlank)
                ? Optional.empty()
                : Optional.of(new SearchQuery(rawTerms, mode));
    }

    public String describe() {
        String words = terms.stream()
                .map(term -> "«" + term + "»")
                .collect(Collectors.joining(" e "));
        return words + " · " + mode.label();
    }

    public String encode() {
        return mode.name() + SEPARATOR + String.join(SEPARATOR, terms);
    }

    public static Optional<SearchQuery> decode(String encoded) {
        List<String> parts = List.of(encoded.split(SEPARATOR));
        if (parts.size() < 2) return Optional.empty();

        try {
            return of(parts.subList(1, parts.size()), MatchMode.valueOf(parts.getFirst()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
