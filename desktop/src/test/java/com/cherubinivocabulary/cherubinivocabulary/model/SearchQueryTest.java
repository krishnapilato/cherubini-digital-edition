package com.cherubinivocabulary.cherubinivocabulary.model;

import module java.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchQueryTest {
    @Test
    void keepsAtMostFourNonBlankTerms() {
        var query = new SearchQuery(List.of(" ferro ", "", "legno", "a", "b", "c"), MatchMode.CONTAINED);
        assertEquals(List.of("ferro", "legno", "a", "b"), query.terms());
    }

    @Test
    void blankInputIsNoQuery() {
        assertTrue(SearchQuery.of(List.of(" ", ""), MatchMode.EXACT).isEmpty());
    }

    @Test
    void roundTripsThroughHistoryEncoding() {
        var query = new SearchQuery(List.of("ferro", "legno"), MatchMode.EXACT);
        assertEquals(Optional.of(query), SearchQuery.decode(query.encode()));
    }

    @Test
    void initialSkipsAccentsAndOcrNoise() {
        assertEquals("A", new Lemma(1, "Àbel", "", "", "").initial());
        assertEquals("Z", new Lemma(2, "- Zerb o Zerbid", "", "", "").initial());
    }
}
