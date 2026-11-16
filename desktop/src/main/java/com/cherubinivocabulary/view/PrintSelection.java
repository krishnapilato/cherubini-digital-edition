package com.cherubinivocabulary.view;

import module java.base;
import module javafx.controls;
import com.cherubinivocabulary.Lemma;
import javafx.scene.control.Label;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * The entries chosen for printing and the side panel that lists them.
 */
public final class PrintSelection {
    private final List<Lemma> lemmas;
    private final Set<Long> chosen = new HashSet<>();
    private Runnable changeHandler = () -> { };
    private final ListView<Lemma> list;
    private final Label subtitle;
    private final Button printNowButton;

    public PrintSelection(List<Lemma> lemmas, ListView<Lemma> list, Label subtitle, Button printNowButton, Button clearButton, List<Label> badges) {
        this.lemmas = lemmas;
        this.list = list;
        this.subtitle = subtitle;
        this.printNowButton = printNowButton;
        var count = Bindings.size(list.getItems());
        printNowButton.disableProperty().bind(count.isEqualTo(0));
        clearButton.disableProperty().bind(count.isEqualTo(0));
        badges.forEach(badge -> {
            badge.textProperty().bind(count.asString());
            badge.visibleProperty().bind(count.greaterThan(0));
            badge.managedProperty().bind(badge.visibleProperty());
        });
        list.setCellFactory(_ -> new PrintCell());
        list.setPlaceholder(Ui.wrapped("Nessuna voce scelta. Spunta «Da stampare» sulle voci trovate o nella voce aperta."));
        refresh();
    }

    public void onChange(Runnable action) {
        changeHandler = action;
    }

    public boolean contains(Lemma lemma) {
        return chosen.contains(lemma.id());
    }

    public void toggle(Lemma lemma) {
        if (!chosen.remove(lemma.id())) chosen.add(lemma.id());
        changed();
    }

    public void addAll(List<Lemma> found) {
        // Notify once after the entire selection is ready, not once per entry.
        if (chosen.addAll(found.stream().map(Lemma::id).toList())) changed();
    }

    public void clear() {
        if (chosen.isEmpty()) return;
        chosen.clear();
        changed();
    }

    // In the order of the vocabulary, not in the order they were chosen.
    public List<Lemma> chosenLemmas() {
        return lemmas.stream().filter(this::contains).toList();
    }

    private void changed() {
        refresh();
        changeHandler.run();
    }

    private void refresh() {
        var count = chosen.size();
        list.getItems().setAll(chosenLemmas());
        subtitle.setText(count == 0 ? "Nessuna voce scelta" : Ui.quantity(count, "voce scelta", "voci scelte"));
        printNowButton.setText(count == 0 ? "Stampa" : "Stampa " + Ui.quantity(count, "voce", "voci"));
    }

    private final class PrintCell extends ListCell<Lemma> {
        @Override
        protected void updateItem(Lemma lemma, boolean empty) {
            super.updateItem(lemma, empty);
            if (empty || lemma == null) {
                setGraphic(null);
                return;
            }
            var name = new Label(lemma.lemma());
            name.getStyleClass().add("print-lemma");
            var number = new Label("N° " + lemma.id());
            number.getStyleClass().addAll(Ui.SECONDARY, "small");
            var texts = new VBox(name, number);
            HBox.setHgrow(texts, Priority.ALWAYS);
            var remove = new Button(null, new FontIcon("fth-x"));
            remove.getStyleClass().addAll("flat", "button-icon");
            remove.setTooltip(new Tooltip("Togli dalla stampa"));
            remove.setOnAction(_ -> {
                if (chosen.remove(lemma.id())) changed();
            });
            var row = new HBox(10, texts, remove);
            row.setAlignment(Pos.CENTER_LEFT);
            setGraphic(row);
        }
    }
}
