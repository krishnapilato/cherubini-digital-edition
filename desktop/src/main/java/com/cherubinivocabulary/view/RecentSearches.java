package com.cherubinivocabulary.view;

import module java.base;
import module javafx.controls;
import com.cherubinivocabulary.History;
import javafx.scene.control.Label;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * The recent searches on the start page, as chips that repeat a search or remove it.
 */
public final class RecentSearches {
    private final FlowPane pane;
    private final History history;
    private final Consumer<String> repeat;

    public RecentSearches(FlowPane pane, History history, Consumer<String> repeat) {
        this.pane = pane;
        this.history = history;
        this.repeat = repeat;
    }

    public void remember(String search) {
        history.remember(search);
        refresh();
    }

    public void refresh() {
        var recent = history.recent();
        pane.getChildren().clear();
        Ui.show(pane, !recent.isEmpty());
        if (recent.isEmpty()) return;
        var label = new Label("Recenti");
        label.getStyleClass().add(Ui.SECONDARY);
        pane.getChildren().add(label);
        recent.forEach(text -> pane.getChildren().add(chip(text)));
    }

    private HBox chip(String text) {
        var search = new Button(text, new FontIcon("fth-clock"));
        search.getStyleClass().add("chip-search");
        search.setOnAction(_ -> repeat.accept(text));
        var remove = new Button(null, new FontIcon("fth-x"));
        remove.getStyleClass().add("chip-remove");
        remove.setTooltip(new Tooltip("Togli dalle recenti"));
        remove.setAccessibleText("Togli «" + text + "» dalle recenti");
        var chip = new HBox(search, remove);
        chip.getStyleClass().add("chip");
        chip.setAlignment(Pos.CENTER_LEFT);
        remove.setOnAction(_ -> vanish(chip, text));
        return chip;
    }

    private void vanish(HBox chip, String text) {
        var fade = new FadeTransition(Duration.millis(180), chip);
        fade.setToValue(0);
        var shrink = new ScaleTransition(Duration.millis(180), chip);
        shrink.setToX(0.6);
        shrink.setToY(0.6);
        var vanish = new ParallelTransition(fade, shrink);
        vanish.setOnFinished(_ -> {
            history.forget(text);
            refresh();
        });
        vanish.play();
    }
}
