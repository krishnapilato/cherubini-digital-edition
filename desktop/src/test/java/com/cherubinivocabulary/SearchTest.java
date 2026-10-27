package com.cherubinivocabulary;

import module java.base;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Runs the search cases shared with the web edition, which must pass the same file. */
class SearchTest {
    private static final Cases CASES = JsonMapper.shared().readValue(Path.of("../tests/search-cases.json").toFile(), Cases.class);
    private static final List<Lemma> LEMMAS = Vocabulary.load();

    record FoldCase(String text, String folded) { }

    record QueryCase(String text, boolean exact, List<String> words, List<String> ignored, String description) { }

    record HighlightCase(String text, String query, boolean exact, List<List<Integer>> ranges) { }

    record SearchCase(String text, boolean contained, List<Long> lemmas, List<Long> definitions) { }

    record Cases(String description, List<FoldCase> fold, List<QueryCase> queries, List<HighlightCase> highlights,
                 Map<String, Long> letters, List<SearchCase> searches) { }

    static List<FoldCase> folds() {
        return CASES.fold();
    }

    static List<QueryCase> queries() {
        return CASES.queries();
    }

    static List<HighlightCase> highlights() {
        return CASES.highlights();
    }

    static List<SearchCase> searches() {
        return CASES.searches();
    }

    @ParameterizedTest
    @MethodSource("folds")
    void foldsKeepingTheLength(FoldCase test) {
        assertEquals(test.folded(), Search.fold(test.text()));
        assertEquals(test.text().length(), Search.fold(test.text()).length());
    }

    @ParameterizedTest
    @MethodSource("queries")
    void splitsTheQuery(QueryCase test) {
        var query = Search.Query.of(test.text(), test.exact());
        assertEquals(test.words(), query.words());
        assertEquals(test.ignored(), query.ignored());
        assertEquals(test.description(), query.describe());
    }

    @ParameterizedTest
    @MethodSource("highlights")
    void findsTheRangesToHighlight(HighlightCase test) {
        var found = Search.find(Search.fold(test.text()), Search.Query.of(test.query(), test.exact()));
        assertEquals(test.ranges(), found.stream().map(range -> List.of(range.start(), range.end())).toList());
    }

    @ParameterizedTest
    @MethodSource("searches")
    void searchesTheVocabulary(SearchCase test) {
        var query = Search.Query.of(test.text(), !test.contained());
        assertEquals(test.lemmas(), idsMatching(Lemma::searchableLemma, query));
        assertEquals(test.definitions(), idsMatching(Lemma::searchableDefinition, query));
    }

    @Test
    void loadsEveryEntryInOrder() {
        List<Long> ids = LEMMAS.stream().map(Lemma::id).toList();
        assertEquals(2307, ids.size());
        assertEquals(ids.stream().sorted().toList(), ids);
    }

    @Test
    void filesTheEntriesUnderTheirInitialLetter() {
        var counts = LEMMAS.stream().collect(Collectors.groupingBy(Lemma::initial, TreeMap::new, Collectors.counting()));
        assertEquals(new TreeMap<>(CASES.letters()), counts);
    }

    private static List<Long> idsMatching(Function<Lemma, String> field, Search.Query query) {
        return LEMMAS.stream().filter(lemma -> Search.matches(field.apply(lemma), query)).map(Lemma::id).toList();
    }
}
