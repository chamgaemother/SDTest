package org.jsoup.nodes;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
public class Document_outputSettings_1_Test {

    @Test
    @DisplayName("outputSettings(null) throws IllegalArgumentException due to null validation")
    public void test_TC03() {
        // GIVEN: a new Document with a valid base URI, so doc is non-null and in initial state (path B0)
        Document doc = new Document("http://example.com");
        // WHEN & THEN: calling outputSettings with null should trigger Validate.notNull and throw IllegalArgumentException (path B1 -> B2)
        assertThrows(IllegalArgumentException.class, () -> {
            doc.outputSettings(null);
        });
    }
}