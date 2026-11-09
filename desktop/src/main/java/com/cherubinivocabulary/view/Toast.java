package com.cherubinivocabulary.view;

import module javafx.graphics;
import javafx.scene.control.Label;
import javafx.util.Duration;

public final class Toast {
    private Toast() {}

    public static void show(Label toast, String message) {
        toast.setText(message);
        toast.setOpacity(0);
        toast.setVisible(true);
        var appear = new FadeTransition(Duration.millis(160), toast);
        appear.setToValue(1);
        var disappear = new FadeTransition(Duration.millis(400), toast);
        disappear.setToValue(0);
        var animation = new SequentialTransition(appear, new PauseTransition(Duration.seconds(2.6)), disappear);
        animation.setOnFinished(_ -> toast.setVisible(false));
        animation.play();
    }
}
