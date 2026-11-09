package com.cherubinivocabulary;

import module java.base;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Application must not leave copies of the guide piling up in the temporary folder.
 */
class TemporaryFilesTest {

    @Test
    void theGuideReusesTheSameFolder() {
        var first = Help.prepare();
        var second = Help.prepare();
        assertEquals(first, second);
        assertTrue(Files.isRegularFile(second));
    }
}
