package com.cherubinivocabulary;

import module java.base;
import module java.logging;
import javafx.application.Application;

class Launcher {

    void main(String[] args) {
        Logger.getLogger("javafx").setLevel(Level.SEVERE);
        // The app is in Italian: JavaFX's own words (dialog buttons, the text fields' menu) must be too.
        Locale.setDefault(Locale.ITALY);

        var smokeTest = List.of(args).contains(CherubiniApp.SMOKE_TEST);
        if (smokeTest) Map.of("glass.platform", "Headless", "prism.order", "sw").forEach(System::setProperty);

        Application.launch(CherubiniApp.class, args);

        if (smokeTest && !CherubiniApp.passedTheSmokeTest()) throw new AssertionError("Window failed to display the vocabulary view.");
    }
}
