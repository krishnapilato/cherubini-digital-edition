package com.cherubinivocabulary;

import module java.base;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The app must not leave copies of the database or of the guide piling up in the temporary folder.
 */
class TemporaryFilesTest {
    private static final Path TEMPORARY = Path.of(System.getProperty("java.io.tmpdir"));

    @Test
    void loadingTheVocabularyLeavesNoCopyBehind() throws IOException {
        var before = databaseCopies();
        assertEquals(2307, Vocabulary.load().size());
        assertEquals(before, databaseCopies());
    }

    @Test
    void theGuideReusesTheSameFolder() {
        var first = Help.prepare();
        var second = Help.prepare();
        assertEquals(first, second);
        assertTrue(Files.isRegularFile(second));
    }

    private static long databaseCopies() throws IOException {
        try (Stream<Path> files = Files.list(TEMPORARY)) {
            return files.filter(file -> file.getFileName().toString().matches("cherubini\\d+\\.db")).count();
        }
    }
}
