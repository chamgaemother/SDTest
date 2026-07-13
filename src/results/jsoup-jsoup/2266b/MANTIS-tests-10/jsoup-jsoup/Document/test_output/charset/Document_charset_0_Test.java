package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.XmlDeclaration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
public class Document_charset_0_Test {

    @Test
    @DisplayName("charset(html syntax, no existing meta[charset]) appends new meta charset and removes obsolete meta[name=charset]")
    public void test_TC01() {
        // GIVEN: HTML syntax, no existing meta[charset] nor meta[name=charset]
        Document doc = Document.createShell("http://example.com");
        // WHEN: set charset to UTF-8; updateMetaCharsetElement defaults to false so enable
        doc.updateMetaCharsetElement(true);
        doc.charset(StandardCharsets.UTF_8);
        // THEN: outputSettings charset set, new <meta charset="UTF-8"> added, no meta[name=charset] remains
        assertEquals(StandardCharsets.UTF_8, doc.outputSettings().charset(),
                "Expected outputSettings charset to be UTF-8");
        assertEquals("UTF-8", doc.head().selectFirst("meta[charset]").attr("charset"),
                "Expected new meta[charset] attribute to be 'UTF-8'");
        assertTrue(doc.select("meta[name=charset]").isEmpty(),
                "Expected obsolete meta[name=charset] to be removed");
    }

    @Test
    @DisplayName("charset(html syntax, existing meta[charset]) updates its charset attribute and removes obsolete meta[name=charset]")
    public void test_TC02() {
        // GIVEN: HTML syntax, existing <meta charset> and obsolete <meta name=charset>
        Document doc = Document.createShell("http://example.com");
        doc.updateMetaCharsetElement(true);
        doc.head().appendElement("meta").attr("charset", "ISO-8859-1");
        doc.head().appendElement("meta").attr("name", "charset");
        // WHEN: set charset to US-ASCII
        doc.charset(StandardCharsets.US_ASCII);
        // THEN: outputSettings charset updated, existing meta[charset] updated, obsolete removed
        assertEquals(StandardCharsets.US_ASCII, doc.outputSettings().charset(),
                "Expected outputSettings charset to be US-ASCII");
        assertEquals("US-ASCII", doc.head().selectFirst("meta[charset]").attr("charset"),
                "Expected existing meta[charset] updated to 'US-ASCII'");
        assertTrue(doc.select("meta[name=charset]").isEmpty(),
                "Expected obsolete meta[name=charset] to be removed");
    }

    @Test
    @DisplayName("charset(xml syntax, no existing XmlDeclaration) prepends new xml declaration with version and encoding")
    public void test_TC03() {
        // GIVEN: XML syntax, no existing XmlDeclaration at document start
        Document doc = Document.createShell("http://example.com");
        doc.outputSettings().syntax(OutputSettings.Syntax.xml);
        doc.updateMetaCharsetElement(true);
        // WHEN: set charset to UTF-8
        doc.charset(StandardCharsets.UTF_8);
        // THEN: syntax remains xml and an XmlDeclaration is prepended with encoding and version
        assertEquals(OutputSettings.Syntax.xml, doc.outputSettings().syntax(),
                "Expected syntax to be XML");
        assertFalse(doc.childNodes().isEmpty(), "Expected at least one child node after setting charset");
        assertTrue(doc.childNodes().get(0) instanceof XmlDeclaration,
                "Expected first child to be XmlDeclaration");
        XmlDeclaration decl = (XmlDeclaration) doc.childNodes().get(0);
        assertEquals("UTF-8", decl.attr("encoding"),
                "Expected xml declaration encoding attribute to be 'UTF-8'");
        assertEquals("1.0", decl.attr("version"),
                "Expected xml declaration version attribute to be '1.0'");
    }

    @Test
    @DisplayName("charset(xml syntax, existing XmlDeclaration named xml without version attr) updates encoding and adds version attr")
    public void test_TC04() {
        // GIVEN: XML syntax, existing XmlDeclaration named 'xml' missing version attribute
        Document doc = Document.createShell("http://example.com");
        doc.outputSettings().syntax(OutputSettings.Syntax.xml);
        doc.updateMetaCharsetElement(true);
        XmlDeclaration existing = new XmlDeclaration("xml", false);
        existing.removeAttr("version");
        existing.attr("encoding", "OLD");
        doc.prependChild(existing);
        // WHEN: set charset to ISO-8859-1
        Charset newCs = Charset.forName("ISO-8859-1");
        doc.charset(newCs);
        // THEN: existing declaration updated with new encoding and version is set to '1.0'
        assertTrue(doc.childNodes().get(0) instanceof XmlDeclaration,
                "Expected first child to still be XmlDeclaration");
        XmlDeclaration updated = (XmlDeclaration) doc.childNodes().get(0);
        assertEquals("ISO-8859-1", updated.attr("encoding"),
                "Expected xml declaration encoding to update to 'ISO-8859-1'");
        assertEquals("1.0", updated.attr("version"),
                "Expected xml declaration version to be added as '1.0'");
    }

    @Test
    @DisplayName("charset(html syntax with obsolete meta[name=charset] only appends new meta[charset] and removes obsolete)")
    public void test_TC05() {
        // GIVEN: HTML syntax, only obsolete <meta name=charset>
        Document doc = Document.createShell("http://example.com");
        doc.updateMetaCharsetElement(true);
        doc.head().appendElement("meta").attr("name", "charset");
        // WHEN: set charset to UTF-8
        doc.charset(StandardCharsets.UTF_8);
        // THEN: new <meta charset> appended, obsolete <meta name=charset> removed
        assertEquals("UTF-8", doc.head().selectFirst("meta[charset]").attr("charset"),
                "Expected new meta[charset] attribute to be 'UTF-8'");
        assertTrue(doc.select("meta[name=charset]").isEmpty(),
                "Expected obsolete meta[name=charset] to be removed");
    }
}