package com.cherubinivocabulary;

import module java.base;
import module java.logging;

import javafx.application.Application;

public class Launcher {
    private static final Logger JAVAFX_LOGGER = Logger.getLogger("javafx");

    static void main(String[] args) {
        hideWarningAboutRunningFromASingleJar();
        boolean smokeTest = List.of(args).contains(CherubiniApp.SMOKE_TEST);
        if (smokeTest) useTheHeadlessPlatform();
        Application.launch(CherubiniApp.class, args);
        if (smokeTest && !CherubiniApp.passedTheSmokeTest()) throw new IllegalStateException("La finestra non mostra il vocabolario");
    }

    private static void hideWarningAboutRunningFromASingleJar() {
        JAVAFX_LOGGER.setLevel(Level.SEVERE);
    }

    // The release workflow starts every package with --smoke-test on machines without a screen.
    private static void useTheHeadlessPlatform() {
        System.setProperty("glass.platform", "Headless");
        System.setProperty("prism.order", "sw");
    }
}
