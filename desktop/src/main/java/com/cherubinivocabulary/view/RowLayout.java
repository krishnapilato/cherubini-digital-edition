package com.cherubinivocabulary.view;

import module java.base;
import module javafx.controls;
import com.cherubinivocabulary.Search;

/**
 * Every row of the list has the same height, so the list places its rows exactly however fast
 * it scrolls: the lemma takes one line, the excerpt two, cut with an ellipsis. The fonts and
 * the cell padding mirror app.css.
 */
public final class RowLayout {
    static final Font LEMMA_FONT = Font.font("IBM Plex Sans SmBld", 15);
    static final Font SNIPPET_FONT = Font.font("IBM Plex Sans", 13);
    static final Font SNIPPET_MATCH_FONT = Font.font("IBM Plex Sans SmBld", 13);
    static final int SNIPPET_LINES = 2;
    static final double ROW_SPACING = 2;
    static final double TEXT_INSET = 100;
    private static final double CELL_PADDING = 18;

    private final ListView<?> list;
    private final TextFlow measure = new TextFlow();
    private final double lemmaLine;
    private final double snippetLine;

    public RowLayout(ListView<?> list) {
        this.list = list;
        lemmaLine = lineHeight(LEMMA_FONT);
        snippetLine = lineHeight(SNIPPET_FONT);
    }

    public double cellHeight() {
        return CELL_PADDING + lemmaLine + ROW_SPACING + SNIPPET_LINES * snippetLine;
    }

    List<Text> title(String text, List<Search.Range> ranges) {
        return excerpt(text, ranges, 0, lemmaLine, LEMMA_FONT, LEMMA_FONT);
    }

    List<Text> snippet(String text, List<Search.Range> ranges, int start) {
        return excerpt(text, ranges, start, SNIPPET_LINES * snippetLine, SNIPPET_FONT, SNIPPET_MATCH_FONT);
    }

    private double lineHeight(Font font) {
        var line = new Text("X");
        line.setFont(font);
        measure.getChildren().setAll(line);
        return measure.prefHeight(-1);
    }

    // The longest stretch of text from start that fits in the given height of a list row.
    private List<Text> excerpt(String text, List<Search.Range> ranges, int start, double height, Font font, Font matchFont) {
        var width = Math.max(1, list.getWidth() - TEXT_INSET);
        // No glyph is narrower than about a quarter of the font size: longer stretches never fit.
        var limit = Math.min(text.length(), start + (int) (height * width / (font.getSize() * font.getSize() / 4)));
        var fitting = start;
        var beyond = limit + 1;
        while (beyond - fitting > 1) {
            var middle = (fitting + beyond) >>> 1;
            if (fits(window(text, ranges, start, middle), height, font, matchFont, width)) fitting = middle;
            else beyond = middle;
        }
        return window(text, ranges, start, fitting);
    }

    private boolean fits(List<Text> parts, double height, Font font, Font matchFont, double width) {
        parts.forEach(part -> part.setFont(part.getStyleClass().contains("match") ? matchFont : font));
        measure.getChildren().setAll(parts);
        return measure.prefHeight(width) <= height + 0.5;
    }

    private static List<Text> window(String text, List<Search.Range> ranges, int start, int end) {
        var visible = ranges.stream()
                .filter(range -> range.start() < end && range.end() > start)
                .map(range -> new Search.Range(Math.max(range.start(), start) - start, Math.min(range.end(), end) - start))
                .toList();
        var parts = new ArrayList<>(Highlight.texts(text.substring(start, end), visible));
        if (start > 0) parts.addFirst(new Text("…"));
        if (end < text.length()) parts.add(new Text("…"));
        return parts;
    }
}
