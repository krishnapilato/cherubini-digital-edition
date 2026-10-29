package com.cherubinivocabulary;

import module java.base;

import javafx.application.HostServices;

final class Help {
    private static final List<String> FILES = List.of(
            "help/index.html", "help/help.css", "help/help.js", "help/gsap.min.js", "help/ScrollTrigger.min.js",
            "icon.png", "fonts/IBMPlexSans-Regular.ttf", "fonts/IBMPlexSans-SemiBold.ttf");

    private Help() {}

    static void open(HostServices host) {
        host.showDocument(prepare().toUri().toString());
    }

    /** Copies the guide into the same temporary folder every time, so copies never pile up. */
    static Path prepare() {
        try {
            Path folder = Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir"), "cherubini-guida"));
            for (String file : FILES) {
                try (InputStream source = Objects.requireNonNull(Help.class.getResourceAsStream(file), file)) {
                    Files.copy(source, folder.resolve(Path.of(file).getFileName()), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            return folder.resolve("index.html");
        } catch (IOException e) {
            throw new UncheckedIOException("Impossibile aprire la guida", e);
        }
    }
}
