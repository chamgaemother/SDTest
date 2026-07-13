package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Entities.EscapeMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class Document_clone_2_Test {

    @Test
    @DisplayName("clone() copies a Document with xml output syntax and escapeMode set to xhtml and isolates subsequent changes")
    public void test_TC07() {
        // GIVEN: an original document with XML syntax, extended escape mode, and prettyPrint disabled
        Document orig = new Document("http://example.com");
        // syntax(xml) branch sets EscapeMode.xhtml internally, then override to extended
        orig.outputSettings()
            .syntax(OutputSettings.Syntax.xml)  // triggers xml path setting of escapeMode to xhtml
            .escapeMode(EscapeMode.extended)
            .prettyPrint(false);

        // WHEN: performing a deep clone
        Document copy = orig.clone();

        // THEN: clone should preserve xml syntax and, per intended behavior, reset escapeMode to xhtml,
        // and preserve prettyPrint=false
        assertEquals(OutputSettings.Syntax.xml, copy.outputSettings().syntax(),
            "Expected clone to have XML syntax independent of original after mutation");
        assertEquals(EscapeMode.xhtml, copy.outputSettings().escapeMode(),
            "Expected clone to have escapeMode XHTML for XML syntax per design");
        assertEquals(false, copy.outputSettings().prettyPrint(),
            "Expected clone to preserve prettyPrint setting");

        // MUTATE original settings to HTML syntax and enable pretty printing
        orig.outputSettings()
            .syntax(OutputSettings.Syntax.html)
            .prettyPrint(true);

        // THEN: clone remains unchanged, verifying deep copy
        assertEquals(OutputSettings.Syntax.xml, copy.outputSettings().syntax(),
            "Clone's syntax should remain XML after original is mutated");
        assertEquals(false, copy.outputSettings().prettyPrint(),
            "Clone's prettyPrint should remain false after original is mutated");
    }

    @Test
    @DisplayName("clone() copies a Document with customized indentAmount and maxPaddingWidth and preserves them independently")
    public void test_TC08() {
        // GIVEN: a shell document with custom indent and padding bounds
        Document orig = Document.createShell("baseUri");
        orig.outputSettings()
            .indentAmount(5)    // branch for valid indent >= 0
            .maxPaddingWidth(100); // branch for valid maxPaddingWidth >= -1

        // WHEN: performing a deep clone
        Document copy = orig.clone();

        // THEN: clone should preserve indentAmount and maxPaddingWidth
        assertEquals(5, copy.outputSettings().indentAmount(),
            "Expected clone to preserve custom indentAmount");
        assertEquals(100, copy.outputSettings().maxPaddingWidth(),
            "Expected clone to preserve custom maxPaddingWidth");

        // MUTATE original beyond bounds: indent=0, padding=-1
        orig.outputSettings()
            .indentAmount(0)      // edge case indent == 0
            .maxPaddingWidth(-1); // edge case padding == -1 unlimited

        // THEN: clone remains unchanged, verifying deep copy unaffected by original's edge-case changes
        assertEquals(5, copy.outputSettings().indentAmount(),
            "Clone's indentAmount should remain 5 after original mutation");
        assertEquals(100, copy.outputSettings().maxPaddingWidth(),
            "Clone's maxPaddingWidth should remain 100 after original mutation");
    }
}