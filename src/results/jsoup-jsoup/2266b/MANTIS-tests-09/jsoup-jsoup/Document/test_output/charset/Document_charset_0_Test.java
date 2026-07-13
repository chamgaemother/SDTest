package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import static org.junit.jupiter.api.Assertions.*;

public class Document_charset_0_Test {

    @Test
    @DisplayName("charset(Charset) with updateMetaCharset=false should only update outputSettings.charset without adding or modifying meta charset element")
    public void test_TC01() {
        // GIVEN a shell document with meta update disabled
        Charset cs = StandardCharsets.ISO_8859_1;
        Document doc = Document.createShell("http://example.com");
        doc.updateMetaCharsetElement(false);
        // WHEN setting charset
        doc.charset(cs);
        // THEN outputSettings charset is updated...
        assertEquals(cs, doc.outputSettings().charset());
        // ...and no <meta charset> element is added (branch B2 taken)
        assertNull(doc.head().selectFirst("meta[charset]"));
    }

    @Test
    @DisplayName("charset(Charset) with updateMetaCharset=true, html syntax, no existing meta[charset] and no obsolete meta[name=charset] should append new meta charset element")
    public void test_TC02() {
        // GIVEN a shell document with meta update enabled, html syntax, and no meta tags yet (loop-0)
        Charset cs = StandardCharsets.UTF_8;
        Document doc = Document.createShell("http://example.com");
        doc.updateMetaCharsetElement(true);
        // WHEN setting charset
        doc.charset(cs);
        // THEN a new <meta charset> element is appended under head (branch B3→B4)
        Element m = doc.head().selectFirst("meta[charset]");
        assertNotNull(m, "Expected a new meta[charset] element");
        assertEquals(cs.displayName(), m.attr("charset"));
    }

    @Test
    @DisplayName("charset(Charset) with updateMetaCharset=true, html syntax, existing meta[charset] plus obsolete meta[name=charset] should update charset attr and remove obsolete element")
    public void test_TC03() {
        // GIVEN a shell document with meta update enabled, html syntax, and both existing <meta charset> and obsolete <meta name=charset>
        Charset cs = StandardCharsets.US_ASCII;
        Document doc = Document.createShell("http://example.com");
        doc.updateMetaCharsetElement(true);
        doc.head().appendElement("meta").attr("charset", "ASCII");
        doc.head().appendElement("meta").attr("name", "charset");
        // WHEN setting charset
        doc.charset(cs);
        // THEN existing charset attr is updated and obsolete removed (branch B4 update path)
        Element updated = doc.head().selectFirst("meta[charset]");
        assertNotNull(updated, "Expected existing meta[charset] to remain");
        assertEquals(cs.displayName(), updated.attr("charset"));
        Elements obsolete = doc.head().select("meta[name=charset]");
        assertTrue(obsolete.isEmpty(), "Expected obsolete meta[name=charset] to be removed");
    }

    @Test
    @DisplayName("charset(Charset) with updateMetaCharset=true, xml syntax, no existing XmlDeclaration child should prepend new xml declaration with encoding and version")
    public void test_TC04() {
        // GIVEN a shell document with meta update enabled and xml syntax, and no xml declaration nodes
        Charset cs = StandardCharsets.UTF_8;
        Document doc = Document.createShell("http://example.com");
        doc.updateMetaCharsetElement(true);
        doc.outputSettings().syntax(OutputSettings.Syntax.xml);
        // WHEN setting charset
        doc.charset(cs);
        // THEN a new XmlDeclaration is prepended as first child with encoding and version (branch B5 xml-syntax)
        Node n = doc.childNodes().get(0);
        assertTrue(n instanceof XmlDeclaration, "Expected first node to be XmlDeclaration");
        XmlDeclaration xd = (XmlDeclaration) n; // Change made here
        assertEquals("xml", xd.name());
        assertEquals(cs.displayName(), xd.attr("encoding"));
        assertEquals("1.0", xd.attr("version"));
    }

    @Test
    @DisplayName("charset(Charset) with updateMetaCharset=true, xml syntax, existing non-xml XmlDeclaration child should replace with xml declaration")
    public void test_TC05() {
        // GIVEN a shell document with meta update enabled, xml syntax, and a non-xml XmlDeclaration at head
        Charset cs = StandardCharsets.UTF_8;
        Document doc = Document.createShell("http://example.com");
        doc.updateMetaCharsetElement(true);
        doc.outputSettings().syntax(OutputSettings.Syntax.xml);
        XmlDeclaration dummy = new XmlDeclaration("notxml", false).attr("dummy", "1");
        doc.prependChild(dummy);
        // WHEN setting charset
        doc.charset(cs);
        // THEN non-xml is replaced by xml declaration at index 0 (branch B5 xml-syntax existing-non-xml)
        Node n = doc.childNodes().get(0);
        assertTrue(n instanceof XmlDeclaration, "Expected replaced XmlDeclaration");
        XmlDeclaration xd = (XmlDeclaration) n; // Change made here
        assertEquals("xml", xd.name());
        assertEquals(cs.displayName(), xd.attr("encoding"));
    }

    @Test
    @DisplayName("charset(Charset) with updateMetaCharset=true, xml syntax, existing xml XmlDeclaration with version attr should update encoding and retain version")
    public void test_TC06() {
        // GIVEN a shell document with meta update enabled, xml syntax, and existing xml declaration with custom version
        Charset cs = StandardCharsets.UTF_8;
        Document doc = Document.createShell("http://example.com");
        doc.updateMetaCharsetElement(true);
        doc.outputSettings().syntax(OutputSettings.Syntax.xml);
        XmlDeclaration existing = new XmlDeclaration("xml", false).attr("version", "2.0");
        doc.prependChild(existing);
        // WHEN setting charset
        doc.charset(cs);
        // THEN existing xml decl is updated: encoding set and version normalized to "1.0" (branch B5 xml-syntax existing-xml)
        Node n = doc.childNodes().get(0);
        assertTrue(n instanceof XmlDeclaration);
        XmlDeclaration xd = (XmlDeclaration) n; // Change made here
        assertEquals("xml", xd.name());
        assertEquals(cs.displayName(), xd.attr("encoding"));
        assertEquals("1.0", xd.attr("version"), "Version should be reset to 1.0");
    }
}