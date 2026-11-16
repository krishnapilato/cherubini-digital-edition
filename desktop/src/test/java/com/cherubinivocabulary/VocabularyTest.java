package com.cherubinivocabulary;

import module java.base;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VocabularyTest {

    @Test
    void readsEveryEntryInTheOrderOfTheVocabulary() {
        var lemmas = Vocabulary.load();
        assertEquals(2307, lemmas.size());
        assertEquals("Abàcch", lemmas.getFirst().lemma());
        assertEquals("T. arit. Abbaco.", lemmas.getFirst().definition());
        assertTrue(IntStream.range(1, lemmas.size()).allMatch(i -> lemmas.get(i - 1).id() < lemmas.get(i).id()));
    }
}
