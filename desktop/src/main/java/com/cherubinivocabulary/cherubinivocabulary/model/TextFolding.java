package com.cherubinivocabulary.cherubinivocabulary.model;

import module java.base;

public final class TextFolding {
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}");

    private TextFolding() {}

    public static String fold(String text) {
        return foldCharacters(text.strip());
    }

    public static String initialOf(String text) {
        return fold(text).codePoints()
                .filter(Character::isLetter)
                .mapToObj(Character::toString)
                .findFirst()
                .orElse("");
    }

    public static FoldedText foldWithIndex(String text) {
        var folded = new StringBuilder(text.length());
        var origin = new int[text.length() + 16];
        int length = 0;
        for (int i = 0; i < text.length(); i++) {
            String piece = foldCharacters(String.valueOf(text.charAt(i)));
            for (int k = 0; k < piece.length(); k++) {
                if (length == origin.length) origin = Arrays.copyOf(origin, origin.length * 2);
                folded.append(piece.charAt(k));
                origin[length++] = i;
            }
        }
        return new FoldedText(folded.toString(), Arrays.copyOf(origin, length));
    }

    private static String foldCharacters(String text) {
        String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD);
        return DIACRITICS.matcher(decomposed).replaceAll("").toUpperCase(Locale.ITALIAN);
    }

    public static final class FoldedText {
        private final String folded;
        private final int[] origin;

        private FoldedText(String folded, int[] origin) {
            this.folded = folded;
            this.origin = origin;
        }

        public String text() {
            return folded;
        }

        public TextRange toOriginal(int start, int end) {
            return new TextRange(origin[start], origin[end - 1] + 1);
        }
    }
}
