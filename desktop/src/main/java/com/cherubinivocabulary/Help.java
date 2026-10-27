package com.cherubinivocabulary;

import module java.base;

import javafx.application.HostServices;

final class Help {
    private static final List<String> FILES = List.of(
            "help/index.html", "help/help.css", "help/help.js", "help/gsap.min.js", "help/ScrollTrigger.min.js",
            "icon.png", "fonts/IBMPlexSans-Regular.ttf", "fonts/IBMPlexSans-SemiBold.ttf");

    private Help() {}

    static void open(HostServices host) {
        try {
            Path folder = Files.createTempDirectory("cherubini-guida");
            for (String file : FILES) {
                try (InputStream source = Objects.requireNonNull(Help.class.getResourceAsStream(file), file)) {
                    Files.copy(source, folder.resolve(Path.of(file).getFileName()));
                }
            }
            host.showDocument(folder.resolve("index.html").toUri().toString());
        } catch (IOException e) {
            throw new UncheckedIOException("Impossibile aprire la guida", e);
        }
    }
}