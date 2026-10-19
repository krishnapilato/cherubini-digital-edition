package com.cherubinivocabulary.cherubinivocabulary.search;

import module java.base;

import com.cherubinivocabulary.cherubinivocabulary.model.Lemma;
import com.cherubinivocabulary.cherubinivocabulary.model.MatchMode;
import com.cherubinivocabulary.cherubinivocabulary.model.SearchQuery;
import com.cherubinivocabulary.cherubinivocabulary.model.TextRange;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VocabularySearchTest {
    private static final Lemma CHIAVE = new Lemma(1, "Ciàv", "", "", "Chiave di ferro con ingegni; serratura di legno ferroso.");
    private static final Lemma ABACCH = new Lemma(2, "Abàcch", "", "", "T. arit. Abbaco.");

    private static SearchQuery query(MatchMode mode, String... terms) {
        return new SearchQuery(List.of(terms), mode);
    }

    @Test
    void containedMatchFindsWordRoots() {
        assertTrue(VocabularySearch.matches(CHIAVE.definition(), query(MatchMode.CONTAINED, "ferro")));
        assertEquals(2, VocabularySearch.occurrences(CHIAVE.definition(), query(MatchMode.CONTAINED, "ferro")).size());
    }

    @Test
    void exactMatchRequiresWholeWords() {
        var ranges = VocabularySearch.occurrences(CHIAVE.definition(), query(MatchMode.EXACT, "ferro"));
        assertEquals(List.of(new TextRange(10, 15)), ranges);
        assertFalse(VocabularySearch.matches(CHIAVE.definition(), query(MatchMode.EXACT, "ferr")));
    }

    @Test
    void allTermsMustOccur() {
        assertTrue(VocabularySearch.matches(CHIAVE.definition(), query(MatchMode.CONTAINED, "ferro", "legno")));
        assertFalse(VocabularySearch.matches(CHIAVE.definition(), query(MatchMode.CONTAINED, "ferro", "rame")));
    }

    @Test
    void ignoresCaseAndAccents() {
        assertTrue(VocabularySearch.matches(ABACCH.lemma(), query(MatchMode.EXACT, "ABACCH")));
        assertEquals(List.of(new TextRange(0, 6)), VocabularySearch.occurrences(ABACCH.lemma(), query(MatchMode.EXACT, "abacch")));
    }

    @Test
    void splitsResultsBetweenLemmasAndDefinitions() {
        var inLemma = VocabularySearch.search(List.of(CHIAVE, ABACCH), query(MatchMode.CONTAINED, "abacch"));
        assertEquals(List.of(ABACCH), inLemma.inLemmas());
        assertEquals(List.of(), inLemma.inDefinitions());

        var inDefinition = VocabularySearch.search(List.of(CHIAVE, ABACCH), query(MatchMode.EXACT, "abbaco"));
        assertEquals(List.of(), inDefinition.inLemmas());
        assertEquals(List.of(ABACCH), inDefinition.inDefinitions());
    }

    @Test
    void mergesOverlappingOccurrences() {
        var ranges = VocabularySearch.occurrences("ferroso", query(MatchMode.CONTAINED, "ferro", "rros"));
        assertEquals(List.of(new TextRange(0, 6)), ranges);
    }
}
