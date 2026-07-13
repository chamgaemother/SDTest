package org.jsoup.nodes;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Document_outputSettings_0_Test {

    @Test
    @DisplayName("outputSettings(non-null) sets the document's outputSettings field and returns this when Validate.notNull passes")
    public void test_TC01() {
        // GIVEN a fresh Document and a new, non-null OutputSettings
        Document doc = new Document("base");
        OutputSettings newSettings = new OutputSettings();
        
        // WHEN calling outputSettings with a valid (non-null) argument
        Document result = doc.outputSettings(newSettings);
        
        // THEN: it should return the same Document instance and update its outputSettings field
        // Inline comment: path B0→B1→B2→B3→B4 means Validate.notNull passes (branch-false for null check)
        assertSame(doc, result, "Expected method to return the same Document instance");
        assertSame(newSettings, doc.outputSettings(), "Expected the document's outputSettings to be updated to the provided instance");
    }

    @Test
    @DisplayName("outputSettings(null) throws IllegalArgumentException when Validate.notNull fails")
    public void test_TC02() {
        // GIVEN a fresh Document
        Document doc = new Document("base");
        
        // WHEN calling outputSettings with null
        // THEN: it should throw IllegalArgumentException, with no mutation of state
        // Inline comment: path B0→B1→B5 means Validate.notNull fails (branch-true for null check)
        IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> doc.outputSettings(null),
            "Expected outputSettings(null) to throw IllegalArgumentException"
        );
        
        // Additionally ensure state was not mutated: outputSettings() should still be default settings
        OutputSettings current = doc.outputSettings();
        assertNotNull(current, "Document should still have a non-null outputSettings after exception");
        // The default should not be the same as null, and be a new instance
        assertTrue(current instanceof OutputSettings, "Expected a valid OutputSettings instance");
    }
}