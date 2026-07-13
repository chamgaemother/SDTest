package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Document.clone method based on provided scenarios.
 */
public class Document_clone_0_Test {

    @Test
    @DisplayName("clone() returns a distinct Document instance with cloned outputSettings and parser")
    void test_TC01() {
        // GIVEN a simple Document shell with known structure (html, head, body)
        Document doc = Document.createShell("http://example.com");
        // WHEN cloning the document
        Document cloned = doc.clone();
        // THEN the cloned document should be a different instance
        assertNotSame(doc, cloned, "clone() must return a new Document instance, not the same reference");
        // AND the outputSettings field should be deep-copied (distinct instances)
        assertNotSame(doc.outputSettings(), cloned.outputSettings(),
                "outputSettings of the clone must not reference the original's outputSettings");
        // AND the parser field should be deep-copied (distinct instances)
        assertNotSame(doc.parser(), cloned.parser(),
                "parser of the clone must not reference the original's parser");
        // AND other observable fields should be equal in value
        assertAll("Other fields equality",
            () -> assertEquals(doc.location(), cloned.location(),
                    "location should be equal in clone"),
            () -> assertEquals(doc.quirksMode(), cloned.quirksMode(),
                    "quirksMode should be preserved in clone"),
            () -> assertEquals(doc.childNodes().size(), cloned.childNodes().size(),
                    "childNodes count should match in clone"),
            () -> assertEquals(doc.attributes(), cloned.attributes(),
                    "attributes should be equal in clone (same values)"));
    }

    @Test
    @DisplayName("modifying the clone's outputSettings does not affect original Document")
    void test_TC02() {
        // GIVEN a Document with default prettyPrint=true
        Document doc = Document.createShell("http://example.com");
        assertTrue(doc.outputSettings().prettyPrint(),
                "Precondition: original document prettyPrint should default to true");
        // WHEN cloning and changing the clone's prettyPrint to false
        Document cloned = doc.clone();
        cloned.outputSettings().prettyPrint(false);
        // THEN the original should remain unaffected
        assertTrue(doc.outputSettings().prettyPrint(),
                "Changing clone's outputSettings.prettyPrint should not affect original document");
        // AND the clone's setting should reflect the change
        assertFalse(cloned.outputSettings().prettyPrint(),
                "Clone's outputSettings.prettyPrint should be false after modification");
    }
}