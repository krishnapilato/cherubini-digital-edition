package com.cherubinivocabulary.cherubinivocabulary.search;

import module java.base;

import com.cherubinivocabulary.cherubinivocabulary.model.Lemma;
import com.cherubinivocabulary.cherubinivocabulary.model.MatchMode;
import com.cherubinivocabulary.cherubinivocabulary.model.SearchQuery;
import com.cherubinivocabulary.cherubinivocabulary.model.SearchResult;
import com.cherubinivocabulary.cherubinivocabulary.model.TextFolding;
import com.cherubinivocabulary.cherubinivocabulary.model.TextRange;

public final class VocabularySearch {
    private VocabularySearch() {
    }

    public static SearchResult search(Collection<Lemma> lemmas, SearchQuery query) {
        return new SearchResult(
                lemmas.stream().filter(lemma -> matches(lemma.lemma(), query)).toList(),
                lemmas.stream().filter(lemma -> matches(lemma.definition(), query)).toList());
    }

    public static boolean matches(String text, SearchQuery query) {
        String folded = TextFolding.foldWithIndex(text).text();
        return query.terms().stream()
                .map(TextFolding::fold)
                .allMatch(term -> indexOf(folded, term, query.mode(), 0) >= 0);
    }

    public static List<TextRange> occurrences(String text, SearchQuery query) {
        var foldedText = TextFolding.foldWithIndex(text);
        String folded = foldedText.text();

        List<TextRange> ranges = new ArrayList<>();
        for (String term : query.terms().stream().map(TextFolding::fold).toList()) {
            if (term.isEmpty()) continue;
            for (int at = indexOf(folded, term, query.mode(), 0); at >= 0;
                 at = indexOf(folded, term, query.mode(), at + term.length())) {
                ranges.add(foldedText.toOriginal(at, at + term.length()));
            }
        }
        return merge(ranges);
    }

    private static int indexOf(String text, String term, MatchMode mode, int from) {
        for (int at = text.indexOf(term, from); at >= 0; at = text.indexOf(term, at + 1)) {
            if (mode == MatchMode.CONTAINED || isWholeWord(text, at, at + term.length())) return at;
        }
        return -1;
    }

    private static boolean isWholeWord(String text, int start, int end) {
        boolean boundaryBefore = start == 0 || !Character.isLetterOrDigit(text.charAt(start - 1));
        boolean boundaryAfter = end == text.length() || !Character.isLetterOrDigit(text.charAt(end));
        return boundaryBefore && boundaryAfter;
    }

    private static List<TextRange> merge(List<TextRange> ranges) {
        List<TextRange> sorted = ranges.stream().sorted(Comparator.comparingInt(TextRange::start)).toList();
        Deque<TextRange> merged = new ArrayDeque<>();
        for (TextRange range : sorted) {
            if (!merged.isEmpty() && merged.getLast().overlapsOrTouches(range)) {
                merged.addLast(merged.removeLast().union(range));
            } else {
                merged.addLast(range);
            }
        }
        return List.copyOf(merged);
    }
}
