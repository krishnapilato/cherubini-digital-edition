package com.cherubinivocabulary;

import module java.logging;

import javafx.application.Application;

public class Launcher {
    private static final Logger JAVAFX_LOGGER = Logger.getLogger("javafx");

    static void main(String[] args) {
        hideWarningAboutRunningFromASingleJar();
        Application.launch(CherubiniApp.class, args);
    }

    private static void hideWarningAboutRunningFromASingleJar() {
        JAVAFX_LOGGER.setLevel(Level.SEVERE);
    }
}