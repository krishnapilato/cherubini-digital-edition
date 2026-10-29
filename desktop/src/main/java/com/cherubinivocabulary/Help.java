package com.cherubinivocabulary;

import module java.base;
import javafx.application.HostServices;

final class Help {
    private Help() {}

    static void open(HostServices host) {
        host.showDocument(prepare().toUri().toString());
    }

    static Path prepare() {
        try {
            var dir = Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir"), "cherubini-guida"));
            for (var f : List.of("help/index.html", "help/help.css", "help/help.js", "help/gsap.min.js", "help/ScrollTrigger.min.js", "icon.png", "fonts/IBMPlexSans-Regular.ttf", "fonts/IBMPlexSans-SemiBold.ttf")) {
                try (var in = Help.class.getResourceAsStream(f)) {
                    Files.copy(Objects.requireNonNull(in, f), dir.resolve(Path.of(f).getFileName()), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            return dir.resolve("index.html");
        } catch (IOException e) {
            throw new UncheckedIOException("Impossibile aprire la guida", e);
        }
    }
}
