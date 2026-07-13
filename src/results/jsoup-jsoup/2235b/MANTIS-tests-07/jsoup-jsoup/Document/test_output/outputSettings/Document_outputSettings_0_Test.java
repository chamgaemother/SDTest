package org.jsoup.nodes;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("Document_outputSettings_Test")
public class Document_outputSettings_0_Test {

    @Test
    @DisplayName("Returns the current OutputSettings instance stored in the Document")
    public void test_TC01() {
        // GIVEN: a new Document and a custom OutputSettings instance
        Document doc = new Document("http://example.com");
        Document.OutputSettings customSettings = new Document.OutputSettings();
        // The setter should store the provided customSettings in the Document
        doc.outputSettings(customSettings);

        // WHEN: retrieving the outputSettings
        Document.OutputSettings result = doc.outputSettings();

        // THEN: the returned instance should be exactly the same as the one set
        // Using assertSame to verify object identity, ensuring correct field storage
        assertSame(customSettings, result);
    }
}