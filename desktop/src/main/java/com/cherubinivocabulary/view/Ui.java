package com.cherubinivocabulary.view;

import module java.base;
import module javafx.controls;
import javafx.scene.control.Label;
import javafx.util.Duration;

public final class Ui {
    public static final String SECONDARY = "secondary";

    private Ui() {}

    public static void show(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    public static void fadeIn(Node node) {
        node.setOpacity(0);
        var fade = new FadeTransition(Duration.millis(220), node);
        fade.setToValue(1);
        fade.play();
    }

    public static void focusAtEnd(TextField field) {
        Platform.runLater(() -> {
            field.requestFocus();
            field.end();
        });
    }

    public static boolean insideTextField(Node node) {
        for (var current = node; current != null; current = current.getParent()) {
            if (current instanceof TextInputControl) return true;
        }
        return false;
    }

    public static Label wrapped(String text) {
        var label = new Label(text);
        label.setWrapText(true);
        label.setTextAlignment(TextAlignment.CENTER);
        label.getStyleClass().add(SECONDARY);
        return label;
    }

    public static String format(long number) {
        return String.format(Locale.ITALIAN, "%,d", number);
    }

    public static String quantity(long count, String one, String many) {
        return format(count) + " " + (count == 1 ? one : many);
    }
}
