package com.cherubinivocabulary.cherubinivocabulary.ui;

import module java.base;
import module javafx.controls;

import javafx.scene.control.Label;

public final class SearchSuggestions extends VBox {
    record Suggestion(String text, String detail, Runnable action) {
    }

    record Section(String title, List<Suggestion> suggestions) {
    }

    public SearchSuggestions() {
        getStyleClass().add("suggestions");
        setSpacing(2);
        setManaged(false);
        setVisible(false);
    }

    void show(Region anchor, List<Section> sections) {
        List<Section> filled = sections.stream().filter(section -> !section.suggestions().isEmpty()).toList();
        if (filled.isEmpty()) {
            hide();
            return;
        }
        getChildren().clear();
        filled.forEach(section -> {
            var title = new Label(section.title());
            title.getStyleClass().add("suggestion-title");
            getChildren().add(title);
            section.suggestions().forEach(suggestion -> getChildren().add(row(suggestion)));
        });
        Bounds below = getParent().sceneToLocal(anchor.localToScene(anchor.getBoundsInLocal()));
        double width = below.getWidth();
        setVisible(true);
        applyCss();
        resizeRelocate(below.getMinX(), below.getMaxY() + 4, width, prefHeight(width));
        layout();
        toFront();
    }

    void hide() {
        setVisible(false);
    }

    boolean isShowing() {
        return isVisible();
    }

    private Button row(Suggestion suggestion) {
        var text = new Label(suggestion.text());
        text.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(text, Priority.ALWAYS);
        var detail = new Label(suggestion.detail());
        detail.getStyleClass().add("suggestion-detail");

        var row = new Button();
        row.setGraphic(new HBox(10, text, detail));
        row.getStyleClass().add("suggestion");
        row.setMaxWidth(Double.MAX_VALUE);
        row.setFocusTraversable(false);
        row.setOnAction(_ -> {
            hide();
            suggestion.action().run();
        });
        return row;
    }
}
