package com.cherubinivocabulary.cherubinivocabulary;

import module java.base;

import javafx.application.HostServices;

final class Help {
    private static final Map<String, String> FILES = Map.of(
            "help/index.html", "index.html",
            "help/help.css", "help.css",
            "help/help.js", "help.js",
            "help/gsap.min.js", "gsap.min.js",
            "help/ScrollTrigger.min.js", "ScrollTrigger.min.js",
            "icon.png", "icon.png",
            "fonts/IBMPlexSans-Regular.ttf", "IBMPlexSans-Regular.ttf",
            "fonts/IBMPlexSans-SemiBold.ttf", "IBMPlexSans-SemiBold.ttf");

    private Help() {}

    static void open(HostServices host) {
        try {
            Path folder = Files.createTempDirectory("cherubini-guida");
            for (var file : FILES.entrySet()) {
                try (InputStream source = Objects.requireNonNull(Help.class.getResourceAsStream(file.getKey()), file.getKey())) {
                    Files.copy(source, folder.resolve(file.getValue()));
                }
            }
            host.showDocument(folder.resolve("index.html").toUri().toString());
        } catch (IOException e) {
            throw new UncheckedIOException("Impossibile aprire la guida", e);
        }
    }
}
