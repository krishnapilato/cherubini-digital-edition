package com.cherubinivocabulary.cherubinivocabulary.ui;

import module java.base;
import module javafx.controls;

import org.kordamp.ikonli.javafx.FontIcon;

final class Icons {
    private Icons() {
    }

    static Button button(String icon, String description) {
        var button = new Button();
        button.setGraphic(new FontIcon(icon));
        button.setTooltip(new Tooltip(description));
        button.setAccessibleText(description);
        button.getStyleClass().addAll("subtle", "icon-only");
        return button;
    }
}
