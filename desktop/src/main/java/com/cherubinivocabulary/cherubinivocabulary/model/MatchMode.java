package com.cherubinivocabulary.cherubinivocabulary.model;

public enum MatchMode {
    EXACT("parola esatta"),
    CONTAINED("parola contenuta");

    private final String label;

    MatchMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
