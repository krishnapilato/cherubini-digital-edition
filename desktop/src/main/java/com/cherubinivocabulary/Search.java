package com.cherubinivocabulary;

import module java.base;

final class Search {
    static final int MAX_WORDS = 4;
    // Any Unicode space separates words, non-breaking ones included, as in the web edition.
    private static final Pattern SPACES = Pattern.compile("(?U)\\s+");

    record Query(List<String> words, List<String> ignored, boolean exact) {

        static Query of(String text, boolean exact) {
            var all = Arrays.stream(SPACES.split(text)).filter(word -> !word.isEmpty()).toList();
            var used = all.stream().limit(MAX_WORDS).toList();
            return new Query(used, all.subList(used.size(), all.size()), exact);
        }

        boolean isEmpty() {
            return words.isEmpty();
        }

        String describe() {
            return words.stream().map(word -> "«" + word + "»").collect(Collectors.joining(" e ")) + " · " + (exact ? "parole esatte" : "anche parole contenute");
        }
    }

    record Range(int start, int end) { }

    private Search() {}

    static String fold(String text) {
        var folded = new StringBuilder(text.length());
        for (var character : text.toCharArray()) folded.append(foldCharacter(character));
        return folded.toString();
    }

    static boolean matches(String searchableText, Query query) {
        return query.words().stream().noneMatch(word -> find(searchableText, List.of(word), query.exact()).isEmpty());
    }

    static List<Range> find(String searchableText, Query query) {
        return find(searchableText, query.words(), query.exact());
    }

    private static List<Range> find(String text, List<String> words, boolean exact) {
        var found = new ArrayList<Range>();
        for (var word : words) {
            var term = fold(word);
            // A word made only of accents folds to spaces and would match every space in the text.
            if (term.isBlank()) continue;
            for (var at = text.indexOf(term); at >= 0; at = text.indexOf(term, at + 1)) {
                if (!exact || isWholeWord(text, at, at + term.length())) found.add(new Range(at, at + term.length()));
            }
        }
        return merge(found);
    }

    private static boolean isWholeWord(String text, int start, int end) {
        var before = start == 0 || !Character.isLetterOrDigit(text.charAt(start - 1));
        var after = end == text.length() || !Character.isLetterOrDigit(text.charAt(end));
        return before && after;
    }

    private static List<Range> merge(List<Range> ranges) {
        var merged = new ArrayList<Range>();
        ranges.stream().sorted(Comparator.comparingInt(Range::start)).forEach(range -> {
            if (!merged.isEmpty() && range.start() <= merged.getLast().end()) merged.set(merged.size() - 1, new Range(merged.getLast().start(), Math.max(merged.getLast().end(), range.end())));
            else merged.add(range);
        });
        return merged;
    }

    private static char foldCharacter(char character) {
        var decomposed = Normalizer.normalize(String.valueOf(character), Normalizer.Form.NFD);
        var base = decomposed.charAt(0);
        return Character.getType(base) == Character.NON_SPACING_MARK ? ' ' : Character.toLowerCase(base);
    }
}
