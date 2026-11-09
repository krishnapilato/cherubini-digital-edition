package com.cherubinivocabulary.view;

import module java.base;
import module javafx.controls;
import com.cherubinivocabulary.Lemma;
import com.cherubinivocabulary.Search;

/**
 * A row of the list: the print box, the lemma and an excerpt of its definition from the first match.
 */
public final class LemmaCell extends ListCell<Lemma> {
    private final RowLayout layout;
    private final PrintSelection print;
    private final Function<String, List<Search.Range>> matches;
    private final BooleanSupplier printable;
    private final CheckBox printBox = new CheckBox();
    private final TextFlow title = new TextFlow();
    private final TextFlow snippet = new TextFlow();
    private final HBox content;

    /**
     * @param matches   the matches of the current search in a searchable text
     * @param printable whether the rows offer the print box
     */
    public LemmaCell(ListView<Lemma> list, RowLayout layout, PrintSelection print, Function<String, List<Search.Range>> matches, BooleanSupplier printable) {
        this.layout = layout;
        this.print = print;
        this.matches = matches;
        this.printable = printable;
        title.getStyleClass().add("list-lemma");
        snippet.getStyleClass().add("list-snippet");
        title.prefWidthProperty().bind(list.widthProperty().subtract(RowLayout.TEXT_INSET));
        snippet.prefWidthProperty().bind(list.widthProperty().subtract(RowLayout.TEXT_INSET));
        printBox.setOnAction(_ -> print.toggle(getItem()));
        printBox.setTooltip(new Tooltip("Da stampare"));
        content = new HBox(10, printBox, new VBox(RowLayout.ROW_SPACING, title, snippet));
    }

    @Override
    protected void updateItem(Lemma lemma, boolean empty) {
        super.updateItem(lemma, empty);
        if (empty || lemma == null) {
            setGraphic(null);
            return;
        }
        Ui.show(printBox, printable.getAsBoolean());
        printBox.setSelected(print.contains(lemma));
        title.getChildren().setAll(layout.title(lemma.lemma(), matches.apply(lemma.searchableLemma())));
        var ranges = matches.apply(lemma.searchableDefinition());
        var start = ranges.isEmpty() ? 0 : Math.max(0, ranges.getFirst().start() - 24);
        // A line break would push the excerpt past its two lines; a space keeps the matches in place.
        var oneLine = lemma.definition().replace('\n', ' ').replace('\r', ' ');
        snippet.getChildren().setAll(layout.snippet(oneLine, ranges, start));
        setGraphic(content);
    }
}
