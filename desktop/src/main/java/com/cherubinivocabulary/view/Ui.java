package com.cherubinivocabulary.view;

import module java.base;
import module javafx.controls;
import atlantafx.base.util.Animations;
import javafx.scene.control.Label;
import javafx.util.Duration;

public final class Ui {
    public static final String SECONDARY = "secondary";
    // The same timing as the web edition (web/src/motion.ts): things come in a little slower than they go away.
    public static final Duration ENTER = Duration.millis(500);
    public static final Duration EXIT = Duration.millis(300);

    private Ui() {}

    public static void show(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    public static void fadeIn(Node node) {
        node.setOpacity(0);
        Animations.fadeIn(node, ENTER).play();
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
