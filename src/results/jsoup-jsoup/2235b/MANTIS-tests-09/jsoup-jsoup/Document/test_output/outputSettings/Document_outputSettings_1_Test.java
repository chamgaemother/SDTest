package org.jsoup.nodes;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
public class Document_outputSettings_1_Test {

    @Test
    @DisplayName("outputSettings(null) throws IllegalArgumentException when passed a null OutputSettings (Validate.notNull branch)")
    public void test_TC03() {
        // GIVEN a new Document instance with a valid base URI
        Document doc = new Document("http://example.com");
        // WHEN & THEN: passing null should trigger the Validate.notNull check and throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            doc.outputSettings(null);
        });
    }
}