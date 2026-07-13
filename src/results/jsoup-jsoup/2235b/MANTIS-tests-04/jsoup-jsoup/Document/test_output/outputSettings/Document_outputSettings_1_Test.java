package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Document_outputSettings_1_Test {

    @Test
    @DisplayName("TC03: outputSettings(null) throws IllegalArgumentException for null argument")
    public void test_TC03() {
        // Given: a new Document with a valid baseUri
        Document doc = new Document("http://example.com");
        // When & Then: calling outputSettings(null) should trigger validation failure and throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            doc.outputSettings(null);
        }, "Expected outputSettings(null) to throw IllegalArgumentException due to null argument");
    }

    @Test
    @DisplayName("TC04: outputSettings returns this Document instance to allow chaining")
    public void test_TC04() {
        // Given: a new Document and a fresh OutputSettings instance
        Document doc = new Document("http://example.com");
        Document.OutputSettings os = new Document.OutputSettings();
        // When: setting outputSettings with a non-null argument should succeed
        Document returned = doc.outputSettings(os);
        // Then: the returned value should be the same instance (method chaining support)
        assertSame(doc, returned, "Expected outputSettings(...) to return the same Document instance for chaining");
    }
}