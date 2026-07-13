package org.jsoup.nodes;

import org.jsoup.helper.DataUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;
public class Document_outputSettings_0_Test {

    @Test
    @DisplayName("TC01: outputSettings() returns the default OutputSettings instance when no setter was called")
    public void test_TC01() {
        // GIVEN a new Document with default settings (no outputSettings setter invoked)
        Document doc = new Document("http://example.com");
        // WHEN retrieving the OutputSettings twice
        Document.OutputSettings first = doc.outputSettings();
        Document.OutputSettings second = doc.outputSettings();
        // THEN the same instance should be returned (default state branch)
        assertSame(first, second, "Expected the same default OutputSettings instance on each call");
        // AND the default charset is UTF-8
        assertEquals(DataUtil.UTF_8, first.charset(), "Default charset should be UTF-8");
        // AND pretty printing is enabled by default
        assertTrue(first.prettyPrint(), "Default prettyPrint should be true");
        // AND default syntax is HTML
        assertEquals(Document.OutputSettings.Syntax.html, first.syntax(), "Default syntax should be html");
    }

    @Test
    @DisplayName("TC02: outputSettings() returns the newly set OutputSettings instance after setter invocation")
    public void test_TC02() {
        // GIVEN a new Document and a custom OutputSettings configured differently
        Document doc = new Document("http://example.com");
        // Creating custom OutputSettings instance
        Document.OutputSettings custom = new Document.OutputSettings()
                .charset("ISO-8859-1")  // set a non-default charset
                .prettyPrint(false)       // disable pretty printing
                .syntax(Document.OutputSettings.Syntax.xml); // switch to XML syntax
        // WHEN setting the custom OutputSettings on the document
        // Correctly invoke outputSettings() method and set custom settings
        Document.OutputSettings returned = doc.outputSettings().charset("ISO-8859-1").prettyPrint(false).syntax(Document.OutputSettings.Syntax.xml);
        // THEN outputSettings() should return the same custom instance (post-setter branch)
        assertSame(custom, returned, "Expected the same custom OutputSettings instance after setter");
        // AND charset reflects the custom setting
        assertEquals(Charset.forName("ISO-8859-1"), returned.charset(), "Charset should be ISO-8859-1");
        // AND pretty printing is disabled as set
        assertFalse(returned.prettyPrint(), "prettyPrint should be false for custom settings");
        // AND syntax reflects the custom XML setting
        assertEquals(Document.OutputSettings.Syntax.xml, returned.syntax(), "Syntax should be xml for custom settings");
    }
}