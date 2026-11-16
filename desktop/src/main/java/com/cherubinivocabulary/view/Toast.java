package com.cherubinivocabulary.view;

import module javafx.graphics;
import atlantafx.base.util.Animations;
import javafx.scene.control.Label;
import javafx.util.Duration;

public final class Toast {
    private Toast() {}

    public static void show(Label toast, String message) {
        toast.setText(message);
        toast.setOpacity(0);
        toast.setVisible(true);
        var appear = Animations.fadeIn(toast, Duration.millis(160));
        var disappear = Animations.fadeOut(toast, Duration.millis(400));
        var animation = new SequentialTransition(appear, new PauseTransition(Duration.seconds(2.6)), disappear);
        animation.setOnFinished(_ -> toast.setVisible(false));
        animation.play();
    }
}
