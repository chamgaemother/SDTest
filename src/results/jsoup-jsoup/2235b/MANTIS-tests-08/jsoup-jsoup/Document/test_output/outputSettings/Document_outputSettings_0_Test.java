package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.Charset;

/**
 * JUnit 5 tests for org.jsoup.nodes.Document.outputSettings method (getter and setter).
 */
public class Document_outputSettings_0_Test {

    @Test
    @DisplayName("TC01: Getter returns the default OutputSettings instance without mutation")
    public void test_TC01() {
        // GIVEN a new Document with default outputSettings
        Document doc = new Document("http://example.com");
        // WHEN we retrieve the outputSettings twice
        Document.OutputSettings firstSettings = doc.outputSettings();
        Document.OutputSettings secondSettings = doc.outputSettings();
        // THEN the getter should always return the same non-null instance (no mutation)
        assertNotNull(firstSettings, "Default outputSettings should not be null");
        assertSame(firstSettings, secondSettings,
                "Getter should return the same instance on each call, indicating no new object creation");
    }

    @Test
    @DisplayName("TC02_O1: Setter with non-null OutputSettings updates field and returns this Document")
    public void test_TC02_O1() {
        // GIVEN a Document and a new OutputSettings with changed prettyPrint
        Document doc = new Document("http://base");
        Document.OutputSettings newSettings = new Document.OutputSettings().prettyPrint(false);
        // Precondition: newSettings is distinct from the default
        assertNotSame(doc.outputSettings(), newSettings,
                "Ensure the new settings is different from the default before setting");
        // WHEN we set the new settings
        Document returned = doc.outputSettings(newSettings);
        // THEN the setter should return the same Document instance and update its internal field
        assertSame(doc, returned, "Setter should return the same Document instance (this)");
        assertSame(newSettings, doc.outputSettings(),
                "After setting, doc.outputSettings() should return the newSettings instance");
    }

    @Test
    @DisplayName("TC03_O1: Setter throws IllegalArgumentException when passed null OutputSettings")
    public void test_TC03_O1() {
        // GIVEN a Document with its initial default settings captured
        Document doc = new Document("http://base");
        Document.OutputSettings initialSettings = doc.outputSettings();
        // WHEN calling setter with null, THEN expect IllegalArgumentException and no change to settings
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> doc.outputSettings(null),
                "Passing null to outputSettings should throw IllegalArgumentException");
        // Verify exception message includes indication of null (Validate.notNull)
        String msg = thrown.getMessage();
        assertTrue(msg != null && !msg.isEmpty(),
                "Exception message should be non-empty to indicate which argument was null");
        // The internal outputSettings should remain unchanged
        assertSame(initialSettings, doc.outputSettings(),
                "After exception, the document's outputSettings should remain unchanged");
    }
}