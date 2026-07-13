package org.jsoup.nodes;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test class for Document.outputSettings(OutputSettings) and outputSettings() getter.
 */
public class Document_outputSettings_2_Test {

    @Test
    @DisplayName("TC05: Fluent chaining: setter followed immediately by getter returns the custom instance")
    public void test_TC05() {
        // GIVEN: a new Document with a valid baseUri, and a non-null custom OutputSettings instance
        Document doc = new Document("http://example.com");
        OutputSettings customSettings = new OutputSettings().prettyPrint(false);
        
        // The path B0→B1→B2 implies entering setter (B0), validating non-null (B1), setting field and returning this (B2)
        // WHEN: chaining setter and getter
        Document.OutputSettings result = doc.outputSettings(customSettings).outputSettings();
        
        // THEN: the getter should return exactly the same instance provided to the setter
        assertSame(customSettings, result, "The outputSettings getter should return the exact same instance provided to the setter for fluent chaining.");
    }
}