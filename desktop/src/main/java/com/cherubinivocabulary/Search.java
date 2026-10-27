package com.cherubinivocabulary;

import module java.base;

final class Search {
    static final int MAX_WORDS = 4;

    record Query(List<String> words, List<String> ignored, boolean exact) {

        static Query of(String text, boolean exact) {
            List<String> all = Arrays.stream(text.strip().split("\\s+")).filter(word -> !word.isBlank()).toList();
            List<String> used = all.stream().limit(MAX_WORDS).toList();
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
        for (char character : text.toCharArray()) folded.append(foldCharacter(character));
        return folded.toString();
    }

    static boolean matches(String searchableText, Query query) {
        return query.words().stream().noneMatch(word -> find(searchableText, List.of(word), query.exact()).isEmpty());
    }

    static List<Range> find(String searchableText, Query query) {
        return find(searchableText, query.words(), query.exact());
    }

    private static List<Range> find(String text, List<String> words, boolean exact) {
        List<Range> found = new ArrayList<>();
        for (String word : words) {
            String term = fold(word);
            for (int at = text.indexOf(term); at >= 0 && !term.isEmpty(); at = text.indexOf(term, at + 1)) {
                if (!exact || isWholeWord(text, at, at + term.length())) found.add(new Range(at, at + term.length()));
            }
        }
        return merge(found);
    }

    private static boolean isWholeWord(String text, int start, int end) {
        boolean before = start == 0 || !Character.isLetterOrDigit(text.charAt(start - 1));
        boolean after = end == text.length() || !Character.isLetterOrDigit(text.charAt(end));
        return before && after;
    }

    private static List<Range> merge(List<Range> ranges) {
        List<Range> merged = new ArrayList<>();
        ranges.stream().sorted(Comparator.comparingInt(Range::start)).forEach(range -> {
            if (!merged.isEmpty() && range.start() <= merged.getLast().end()) {
                merged.set(merged.size() - 1, new Range(merged.getLast().start(), Math.max(merged.getLast().end(), range.end())));
            } else {
                merged.add(range);
            }
        });
        return merged;
    }

    private static char foldCharacter(char character) {
        String decomposed = Normalizer.normalize(String.valueOf(character), Normalizer.Form.NFD);
        char base = decomposed.charAt(0);
        if (Character.getType(base) == Character.NON_SPACING_MARK) return ' ';
        return Character.toLowerCase(base);
    }
}
