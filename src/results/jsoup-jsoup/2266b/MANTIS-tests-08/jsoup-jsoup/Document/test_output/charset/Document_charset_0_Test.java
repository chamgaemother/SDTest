package org.jsoup.nodes;

import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.XmlDeclaration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Document_charset_0_Test {

    @Test
    @DisplayName("TC01: charset(Charset.UTF_8) on HTML document with no existing <meta charset> or obsolete tags appends new meta and removes none")
    public void test_TC01() {
        // GIVEN a fresh HTML shell doc: no meta[charset], no obsolete meta[name=charset], updateMetaCharset false by default
        Document doc = Document.createShell("http://example.com");
        // WHEN calling charset -> updateMetaCharsetElement(true) then ensure in HTML branch with no existing meta[charset]
        doc.charset(Charset.forName("UTF-8"));
        // THEN a new meta[charset] is appended with UTF_8, and no obsolete meta[name=charset] remain
        assertEquals(Charset.forName("UTF-8").displayName(),
                     doc.selectFirst("meta[charset]").attr("charset"),
                     "Expected appended meta[charset] to have UTF_8");
        assertTrue(doc.select("meta[name=charset]").isEmpty(),
                   "Expected no obsolete meta[name=charset] elements");
    }

    @Test
    @DisplayName("TC02: charset(Charset.ISO_8859_1) updates existing <meta charset> and removes no obsolete tags")
    public void test_TC02() {
        // GIVEN an HTML shell with an existing meta[charset]=OLD
        Document doc = Document.createShell("http://example.com");
        doc.head().appendElement("meta").attr("charset", "OLD");
        // WHEN calling charset -> updateMetaCharsetElement(true) then ensure in HTML branch with existing meta[charset]
        doc.charset(Charset.forName("ISO-8859-1"));
        // THEN the existing meta[charset] is updated to ISO_8859_1 and no obsolete meta[name=charset]
        assertEquals(Charset.forName("ISO-8859-1").displayName(),
                     doc.selectFirst("meta[charset]").attr("charset"),
                     "Expected existing meta[charset] to be updated to ISO_8859_1");
        assertTrue(doc.select("meta[name=charset]").isEmpty(),
                   "Expected no obsolete meta[name=charset] elements");
    }

    @Test
    @DisplayName("TC03: charset(Charset.UTF_16) on HTML document removes multiple obsolete <meta name=charset> elements")
    public void test_TC03() {
        // GIVEN an HTML shell with two obsolete meta[name=charset] entries
        Document doc = Document.createShell("http://example.com");
        doc.head().appendElement("meta").attr("name", "charset");
        doc.head().appendElement("meta").attr("name", "charset");
        // WHEN calling charset -> updateMetaCharsetElement(true) then HTML branch with no meta[charset]
        doc.charset(Charset.forName("UTF-16"));
        // THEN a new meta[charset] is appended with UTF_16, and all obsolete meta[name=charset] are removed
        assertEquals(Charset.forName("UTF-16").displayName(),
                     doc.selectFirst("meta[charset]").attr("charset"),
                     "Expected appended meta[charset] to have UTF_16");
        assertTrue(doc.select("meta[name=charset]").isEmpty(),
                   "Expected all obsolete meta[name=charset] elements removed");
    }

    @Test
    @DisplayName("TC04: charset(null) throws NullPointerException when passed null charset")
    public void test_TC04() {
        // GIVEN a simple new Document
        Document doc = new Document("http://example.com");
        // WHEN charset(null) is called -> Validate.notNull(title) should throw NullPointerException
        assertThrows(NullPointerException.class, () -> doc.charset(null),
                     "Expected NullPointerException when passing null charset");
        // no meta tags should have been added
        assertTrue(doc.select("meta[charset]").isEmpty(), "Expected no meta[charset] added after null input");
    }

    @Test
    @DisplayName("TC05: charset(Charset.UTF_8) in XML mode with no existing declaration prepends new XmlDeclaration")
    public void test_TC05() {
        // GIVEN an HTML shell, with updateMetaCharsetElement enabled, and syntax set to XML, no XmlDeclaration yet
        Document doc = Document.createShell("http://example.com");
        doc.updateMetaCharsetElement(true);
        doc.outputSettings().syntax(OutputSettings.Syntax.xml);
        // WHEN calling charset -> XML branch with no existing XmlDeclaration
        doc.charset(Charset.forName("UTF-8"));
        // THEN the first child node is a new XmlDeclaration with correct version and encoding
        Node first = doc.childNode(0);
        assertTrue(first instanceof XmlDeclaration, "Expected first node to be XmlDeclaration");
        XmlDeclaration decl = (XmlDeclaration) first;
        assertEquals(Charset.forName("UTF-8").displayName(),
                     decl.attr("encoding"),
                     "Expected XmlDeclaration encoding to be set to UTF_8");
        assertEquals("1.0",
                     decl.attr("version"),
                     "Expected XmlDeclaration version to be '1.0'");
    }

    @Test
    @DisplayName("TC06: charset(Charset.ISO_8859_1) in XML mode updates existing XmlDeclaration encoding and preserves version")
    public void test_TC06() {
        // GIVEN an HTML shell, updateMetaCharsetElement true, syntax xml, and an existing XmlDeclaration with version=1.0 and encoding=OLD
        Document doc = Document.createShell("http://example.com");
        doc.updateMetaCharsetElement(true);
        doc.outputSettings().syntax(OutputSettings.Syntax.xml);
        XmlDeclaration existing = new XmlDeclaration("xml", false);
        existing.attr("version", "1.0");
        existing.attr("encoding", "OLD");
        doc.prependChild(existing);
        // WHEN calling charset -> XML branch with existing XmlDeclaration
        doc.charset(Charset.forName("ISO-8859-1"));
        // THEN the XmlDeclaration encoding is updated, version stays the same
        Node first = doc.childNode(0);
        assertTrue(first instanceof XmlDeclaration, "Expected first node to be XmlDeclaration");
        XmlDeclaration decl = (XmlDeclaration) first;
        assertEquals(Charset.forName("ISO-8859-1").displayName(),
                     decl.attr("encoding"),
                     "Expected XmlDeclaration encoding updated to ISO_8859_1");
        assertEquals("1.0",
                     decl.attr("version"),
                     "Expected XmlDeclaration version preserved as '1.0'");
    }
}