package com.cherubinivocabulary.view;

import module java.base;
import module javafx.graphics;
import com.cherubinivocabulary.Search;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

/**
 * Marks the matches of a search: as text pieces for titles and list rows, as style spans for the open entry.
 */
final class Highlight {
    private Highlight() {}

    static List<Text> texts(String text, List<Search.Range> ranges) {
        var parts = new ArrayList<Text>();
        var cursor = 0;
        for (var range : ranges) {
            if (range.start() > cursor) parts.add(new Text(text.substring(cursor, range.start())));
            var match = new Text(text.substring(range.start(), range.end()));
            match.getStyleClass().add("match");
            parts.add(match);
            cursor = range.end();
        }
        if (cursor < text.length()) parts.add(new Text(text.substring(cursor)));
        return parts;
    }

    static StyleSpans<Collection<String>> spans(int length, List<Search.Range> ranges, int current) {
        var spans = new StyleSpansBuilder<Collection<String>>();
        var cursor = 0;
        for (var i = 0; i < ranges.size(); i++) {
            var range = ranges.get(i);
            spans.add(List.of(), range.start() - cursor);
            spans.add(List.of(i == current ? "hit-current" : "hit"), range.end() - range.start());
            cursor = range.end();
        }
        spans.add(List.of(), length - cursor);
        return spans.create();
    }
}
