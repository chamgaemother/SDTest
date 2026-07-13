package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Document_outputSettings_0_Test {

    @Test
    @DisplayName("outputSettings() returns the default OutputSettings instance when no setter called")
    public void test_TC01() {
        // GIVEN a fresh Document with no outputSettings setter called
        Document doc = new Document("http://example.com");
        // WHEN retrieving the default OutputSettings
        Document.OutputSettings settings1 = doc.outputSettings();
        // THEN the returned settings should be non-null and stable across calls
        assertNotNull(settings1, "Expected non-null default OutputSettings");
        // calling again should return the same instance (no modification, B0→B1→B2 path)
        Document.OutputSettings settings2 = doc.outputSettings();
        assertSame(settings1, settings2, "Expected identical OutputSettings instance on multiple calls");
    }

    @Test
    @DisplayName("outputSettings() returns the newly set OutputSettings instance after setter invocation")
    public void test_TC02() {
        // GIVEN a fresh Document and a new OutputSettings instance with changed prettyPrint
        Document doc = new Document("http://example.com");
        Document.OutputSettings newSettings = new Document.OutputSettings().prettyPrint(false);
        // WHEN setting the new OutputSettings
        Document returnedDoc = doc.outputSettings(newSettings);
        // then the setter returns the Document itself (fluent API)
        assertSame(doc, returnedDoc, "Setter should return the same Document instance for chaining");
        // AND retrieving outputSettings() now should return the exact newSettings instance (setter branch B0→B1→B2)
        Document.OutputSettings settingsAfter = doc.outputSettings();
        assertSame(newSettings, settingsAfter, "Expected outputSettings() to return the newly set instance");
    }
}