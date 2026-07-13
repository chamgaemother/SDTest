package org.jsoup.nodes;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Document_outputSettings_2_Test {

    @Test
    @DisplayName("TC04: outputSettings(null) throws IllegalArgumentException with expected 'must not be null' message")
    public void test_TC04() {
        // GIVEN a new Document with a valid baseUri, to exercise the null-check branch of outputSettings
        Document doc = new Document("http://example.com");

        // WHEN / THEN: calling outputSettings with null should trigger Validate.notNull → IllegalArgumentException
        IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> doc.outputSettings(null),
            "Expected outputSettings(null) to throw, but it didn't"
        );

        // Ensure the exception message indicates that null is not allowed
        assertTrue(
            thrown.getMessage().contains("must not be null"),
            "Exception message should contain 'must not be null'"
        );
    }
}