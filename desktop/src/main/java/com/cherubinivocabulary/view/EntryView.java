package com.cherubinivocabulary.view;

import module java.base;
import module javafx.controls;
import com.cherubinivocabulary.Lemma;
import com.cherubinivocabulary.Search;
import javafx.scene.control.Label;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;

/**
 * The open entry: its number, its lemma and its definition, with the matches marked and the
 * current occurrence followed.
 */
public final class EntryView {
    public static final int NO_OCCURRENCE = -1;

    private final StyleClassedTextArea definition = new StyleClassedTextArea();
    private final Label number;
    private final TextFlow title;
    private final Label occurrenceLabel;
    private final List<Button> occurrenceButtons;
    private int occurrence = NO_OCCURRENCE;

    public EntryView(StackPane holder, Label number, TextFlow title, Label occurrenceLabel, List<Button> occurrenceButtons) {
        this.number = number;
        this.title = title;
        this.occurrenceLabel = occurrenceLabel;
        this.occurrenceButtons = occurrenceButtons;
        definition.setEditable(false);
        definition.setWrapText(true);
        // The entry is for reading: mouse presses and drags would select its text in blue.
        definition.addEventFilter(MouseEvent.ANY, MouseEvent::consume);
        definition.setFocusTraversable(false);
        definition.getStyleClass().add("definition");
        holder.getChildren().add(new VirtualizedScrollPane<>(definition));
    }

    public int currentOccurrence() {
        return occurrence;
    }

    public void clear() {
        occurrence = NO_OCCURRENCE;
        number.setText("");
        title.getChildren().setAll(new Text("Nessuna voce"));
        definition.replaceText("");
        occurrenceLabel.setText("");
    }

    public void show(Lemma lemma, List<Search.Range> lemmaMatches, List<Search.Range> definitionMatches) {
        number.setText("N° " + lemma.id() + " · lettera " + lemma.initial());
        title.getChildren().setAll(Highlight.texts(lemma.lemma(), lemmaMatches));
        showDefinition(lemma, definitionMatches, NO_OCCURRENCE);
    }

    public void showDefinition(Lemma lemma, List<Search.Range> ranges, int current) {
        occurrence = current;
        definition.replaceText(lemma.definition());
        if (!lemma.definition().isEmpty())
            definition.setStyleSpans(0, Highlight.spans(lemma.definition().length(), ranges, occurrence));
        if (occurrence >= 0) {
            definition.moveTo(ranges.get(occurrence).start());
            definition.requestFollowCaret();
        } else {
            definition.moveTo(0);
            definition.showParagraphAtTop(0);
        }
        occurrenceLabel.setText(occurrenceText(ranges.size()));
        occurrenceButtons.forEach(button -> button.setDisable(ranges.isEmpty()));
    }

    private String occurrenceText(int count) {
        if (count == 0) return "Nessuna occorrenza";
        return occurrence < 0 ? Ui.quantity(count, "occorrenza", "occorrenze") : "Occorrenza " + (occurrence + 1) + " di " + count;
    }
}
