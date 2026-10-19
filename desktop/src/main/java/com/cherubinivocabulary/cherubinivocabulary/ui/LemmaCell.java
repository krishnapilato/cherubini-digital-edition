package com.cherubinivocabulary.cherubinivocabulary.ui;

import module java.base;
import module javafx.controls;

import com.cherubinivocabulary.cherubinivocabulary.model.Lemma;
import com.cherubinivocabulary.cherubinivocabulary.model.SearchQuery;
import com.cherubinivocabulary.cherubinivocabulary.model.TextRange;
import com.cherubinivocabulary.cherubinivocabulary.search.VocabularySearch;

final class LemmaCell extends ListCell<Lemma> {
    private static final int SNIPPET_LENGTH = 90;

    private final Supplier<SearchQuery> currentQuery;
    private final ObservableSet<Long> toPrint;
    private final CheckBox printBox = new CheckBox();
    private final MarkedText lemma = new MarkedText();
    private final MarkedText snippet = new MarkedText();
    private final HBox content;

    LemmaCell(ListView<Lemma> list, Supplier<SearchQuery> currentQuery, ObservableSet<Long> toPrint) {
        this.currentQuery = currentQuery;
        this.toPrint = toPrint;
        lemma.setRunStyle("list-lemma");
        snippet.setRunStyle("list-snippet");
        var texts = new VBox(1, lemma, snippet);
        texts.prefWidthProperty().bind(list.widthProperty().subtract(64));
        lemma.prefWidthProperty().bind(texts.prefWidthProperty());
        snippet.prefWidthProperty().bind(texts.prefWidthProperty());
        printBox.setOnAction(_ -> togglePrint());
        printBox.setTooltip(new Tooltip("Da stampare"));
        content = new HBox(10, printBox, texts);
        content.setAlignment(Pos.TOP_LEFT);
    }

    @Override
    protected void updateItem(Lemma item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
            return;
        }
        SearchQuery query = currentQuery.get();
        boolean searching = query != null;
        printBox.setVisible(searching);
        printBox.setManaged(searching);
        printBox.setSelected(toPrint.contains(item.id()));
        lemma.setContent(item.lemma(), searching ? VocabularySearch.occurrences(item.lemma(), query) : List.of(), MarkedText.NO_CURRENT_MATCH);
        showSnippet(item.definition().strip(), searching ? VocabularySearch.occurrences(item.definition().strip(), query) : List.of());
        setGraphic(content);
    }

    private void showSnippet(String definition, List<TextRange> matches) {
        int start = matches.isEmpty() ? 0 : Math.max(0, matches.getFirst().start() - 20);
        int end = Math.min(definition.length(), start + SNIPPET_LENGTH);
        String prefix = start > 0 ? "…" : "";
        String suffix = end < definition.length() ? "…" : "";
        List<TextRange> visible = matches.stream()
                .filter(match -> match.start() >= start && match.end() <= end)
                .map(match -> new TextRange(match.start() - start + prefix.length(), match.end() - start + prefix.length()))
                .toList();
        snippet.setContent(prefix + definition.substring(start, end) + suffix, visible, MarkedText.NO_CURRENT_MATCH);
    }

    private void togglePrint() {
        long id = getItem().id();
        if (!toPrint.remove(id)) toPrint.add(id);
    }
}
