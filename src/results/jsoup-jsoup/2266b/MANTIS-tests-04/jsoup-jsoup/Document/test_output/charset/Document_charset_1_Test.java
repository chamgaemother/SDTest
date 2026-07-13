package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.nio.charset.Charset;
import static org.junit.jupiter.api.Assertions.*;
public class Document_charset_1_Test {

    @Test
    @DisplayName("charset(html, updateMetaCharset=false) does not insert or remove any meta elements")
    public void test_TC08() {
        // Branch B1: updateMetaCharsetElement is false, so ensureMetaCharsetElement should be skipped
        Document doc = Document.createShell("http://example.com");
        doc.updateMetaCharsetElement(false); // disable meta updates

        // WHEN: call charset on HTML syntax (default)
        Charset newCs = Charset.forName("UTF-16");
        doc.charset(newCs);

        // THEN: no <meta charset> added
        assertTrue(doc.select("meta[charset]").isEmpty(),
                "Expected no <meta charset> elements when updateMetaCharsetElement is false");
        // AND: no obsolete <meta name=charset> present
        assertTrue(doc.select("meta[name=charset]").isEmpty(),
                "Expected no <meta name=charset> elements when updateMetaCharsetElement is false");
        // AND: outputSettings.charset is updated
        assertEquals(newCs, doc.charset(),
                "Expected document.charset() to return the newly set charset");
    }

    @Test
    @DisplayName("charset(html, updateMetaCharset=true) updates existing meta[charset] and removes obsolete meta[name=charset] when both present")
    public void test_TC09() {
        // Branch B1: updateMetaCharsetElement true => enter html branch in ensureMetaCharsetElement
        Document doc = Document.createShell("http://example.com");
        doc.updateMetaCharsetElement(true);
        // Precondition: head contains both meta[charset] and obsolete meta[name=charset]
        Element head = doc.head();
        head.appendElement("meta").attr("charset", "ISO-8859-1");
        head.appendElement("meta").attr("name", "charset");

        // WHEN: set new charset
        Charset newCs = Charset.forName("UTF-32");
        doc.charset(newCs);

        // THEN: existing <meta charset> is updated to new charset value
        Element updated = doc.selectFirst("meta[charset]");
        assertNotNull(updated, "Expected a meta[charset] element to exist");
        assertEquals(newCs.displayName(), updated.attr("charset"),
                "Expected meta[charset] attribute to be updated to the new charset");
        // AND: obsolete <meta name=charset> removed
        assertTrue(doc.select("meta[name=charset]").isEmpty(),
                "Expected obsolete <meta name=charset> elements to be removed");
    }

    @Test
    @DisplayName("charset(xml, updateMetaCharset=false) does not prepend or update XmlDeclaration, only outputSettings updated")
    public void test_TC10() {
        // Branch B1: updateMetaCharsetElement is false, xml branch skipped in ensureMetaCharsetElement
        Document doc = new Document("http://example.com");
        // Set syntax to XML
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        doc.updateMetaCharsetElement(false);

        // WHEN: call charset on XML syntax
        Charset newCs = Charset.forName("US-ASCII");
        doc.charset(newCs);

        // THEN: no XmlDeclaration node should be added or modified
        boolean hasXmlDecl = doc.childNodes().stream()
                .anyMatch(n -> n instanceof XmlDeclaration);
        assertFalse(hasXmlDecl,
                "Expected no XmlDeclaration in childNodes when updateMetaCharsetElement is false");
        // AND: outputSettings.charset is updated
        assertEquals(newCs, doc.charset(),
                "Expected document.charset() to return the newly set charset");
    }
}