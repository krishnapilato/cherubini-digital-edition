package com.cherubinivocabulary.cherubinivocabulary.ui;

import module java.base;
import module javafx.controls;

import javafx.scene.control.Label;
import javafx.util.Duration;

final class Animations {
    private static final Duration SETTLE = Duration.millis(280);
    private static final Interpolator EASE_OUT = Interpolator.ofSpline(0.2, 0.8, 0.2, 1.0);

    private Animations() {
    }

    static void settle(Node node, Duration delay, double offsetY) {
        node.setOpacity(0);
        node.setTranslateY(offsetY);

        var fade = new FadeTransition(SETTLE, node);
        fade.setToValue(1);
        fade.setInterpolator(EASE_OUT);

        var slide = new TranslateTransition(SETTLE, node);
        slide.setToY(0);
        slide.setInterpolator(EASE_OUT);

        var transition = new ParallelTransition(fade, slide);
        transition.setDelay(delay);
        transition.play();
    }

    static void settle(Node node) {
        settle(node, Duration.ZERO, 6);
    }

    static void shake(Node node) {
        var shake = new TranslateTransition(Duration.millis(50), node);
        shake.setFromX(0);
        shake.setByX(5);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setOnFinished(_ -> node.setTranslateX(0));
        shake.play();
    }

    static void showMessage(Label label, String text) {
        label.setText(text);
        label.setVisible(true);
        label.setManaged(true);
        settle(label, Duration.ZERO, 3);
    }

    static void hideMessage(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }
}
