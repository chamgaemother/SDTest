package org.jsoup.nodes;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for the Document.outputSettings() getter and setter methods,
 * based on provided scenarios TC01 and TC02.
 */
public class Document_outputSettings_0_Test {

    @Test
    @DisplayName("outputSettings() returns the default OutputSettings instance after Document creation")
    public void test_TC01() {
        // GIVEN: a new Document with default constructor
        Document doc = new Document("http://example.com");
        
        // WHEN: retrieving the outputSettings for the first time (default branch B0->B1)
        OutputSettings settings = doc.outputSettings();
        
        // THEN: the returned object should be the same instance on subsequent calls
        assertSame(settings, doc.outputSettings(),
                "Getter should always return the same default OutputSettings instance");
        // AND: the default charset should be UTF-8 as per default OutputSettings
        assertEquals("UTF-8", settings.charset().name(),
                "Default charset must be UTF-8");
        // AND: the default syntax should be HTML as per default OutputSettings
        assertEquals(Syntax.html, settings.syntax(),
                "Default syntax must be html");
    }

    @Test
    @DisplayName("outputSettings() returns the updated OutputSettings instance after setter call")
    public void test_TC02() {
        // GIVEN: a new Document and a custom OutputSettings instance mutated before setting
        Document doc = new Document("http://example.com");
        OutputSettings newSettings = new OutputSettings()
                .prettyPrint(false)   // disable pretty printing to satisfy mutation branch
                .syntax(Syntax.xml);  // set XML syntax to test setter behavior
        
        // WHEN: setting the document's OutputSettings to our custom instance (still B0->B1 branch)
        doc.outputSettings(newSettings);
        OutputSettings returned = doc.outputSettings();
        
        // THEN: the getter should return the exact same instance passed to the setter
        assertSame(newSettings, returned,
                "Getter should return the instance provided via the setter");
        // AND: ensure our mutations were retained: prettyPrint(false)
        assertFalse(returned.prettyPrint(),
                "After setter, prettyPrint should reflect the custom setting of false");
        // AND: ensure syntax was updated to XML
        assertEquals(Syntax.xml, returned.syntax(),
                "After setter, syntax should reflect the custom setting of xml");
    }
}