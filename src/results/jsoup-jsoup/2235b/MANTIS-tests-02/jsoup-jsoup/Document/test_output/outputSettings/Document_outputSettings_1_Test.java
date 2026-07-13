package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for Document.outputSettings(OutputSettings)
 */
public class Document_outputSettings_1_Test {

    @Test
    @DisplayName("TC03: outputSettings(null) throws IllegalArgumentException due to Validate.notNull")
    public void test_TC03() {
        // GIVEN a new Document instance
        Document doc = new Document("http://example.com");
        // WHEN & THEN: passing null should trigger the Validate.notNull check -> IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            doc.outputSettings(null);
        }, "Expected outputSettings(null) to throw IllegalArgumentException due to null argument");
    }

    @Test
    @DisplayName("TC04: outputSettings(custom) returns the same Document instance for chaining")
    public void test_TC04() {
        // GIVEN a new Document instance and a custom OutputSettings
        Document doc = new Document("http://example.com");
        Document.OutputSettings customSettings = new Document.OutputSettings();
        // WHEN setting the custom settings
        Document returned = doc.outputSettings(customSettings);
        // THEN the returned reference should be the same as the original doc (fluent setter)
        assertSame(doc, returned, "outputSettings should return 'this' for chaining");
    }
}