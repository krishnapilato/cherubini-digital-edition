package com.cherubinivocabulary;

import module java.base;
import module java.logging;
import javafx.application.Application;

public class Launcher {

    static void main(String[] args) {
        Logger.getLogger("javafx").setLevel(Level.SEVERE);

        var smokeTest = List.of(args).contains(CherubiniApp.SMOKE_TEST);
        if (smokeTest) Map.of("glass.platform", "Headless", "prism.order", "sw").forEach(System::setProperty);

        Application.launch(CherubiniApp.class, args);

        if (smokeTest && !CherubiniApp.passedTheSmokeTest()) throw new IllegalStateException("La finestra non mostra il vocabolario");
    }
}
