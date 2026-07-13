package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Document_charset_2_Test {

    @Test
    @DisplayName("charset(Charset.UTF_8) with updateMetaCharset disabled does not modify HTML head")
    public void test_TC09() {
        // GIVEN a basic HTML document shell and updateMetaCharset disabled => branch B1(false)
        Document doc = Document.createShell("http://example.com");
        doc.updateMetaCharsetElement(false);
        // WHEN applying charset change
        doc.charset(Charset.forName("UTF-8"));
        // THEN no <meta charset> or <meta name=charset> tags should be added to head
        assertTrue(doc.select("meta[charset]").isEmpty(), "Expected no meta[charset] elements when updateMetaCharset is false");
        assertTrue(doc.select("meta[name=charset]").isEmpty(), "Expected no meta[name=charset] elements when updateMetaCharset is false");
    }

    @Test
    @DisplayName("charset(Charset.ISO_8859_1) with updateMetaCharset disabled in XML mode does not prepend XmlDeclaration")
    public void test_TC10() {
        // GIVEN a basic HTML document shell, XML syntax, updateMetaCharset disabled => skip ensureMetaCharsetElement
        Document doc = Document.createShell("http://example.com");
        doc.updateMetaCharsetElement(false);
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        // WHEN applying charset change
        doc.charset(Charset.forName("ISO-8859-1"));
        // THEN first child node should remain the <html> element, not an XmlDeclaration
        org.jsoup.nodes.Node first = doc.childNode(0);
        assertFalse(first instanceof XmlDeclaration,
            "Expected first node not to be XmlDeclaration when updateMetaCharset is false in XML mode");
    }

    @Test
    @DisplayName("charset(Charset.UTF_16) in XML mode updates existing XmlDeclaration without version attr only encoding")
    public void test_TC11() {
        // GIVEN an XML document shell with updateMetaCharset enabled and an existing XmlDeclaration lacking 'version'
        Document doc = Document.createShell("http://example.com");
        doc.updateMetaCharsetElement(true); // branch B1(true)
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml); // branch B4(xml)
        // Prepend a declaration node as if existing without version attribute
        XmlDeclaration decl = new XmlDeclaration("xml", false);
        decl.attr("encoding", "OLD"); // ensure has no 'version'
        doc.prependChild(decl);
        // WHEN changing charset to UTF-16
        doc.charset(Charset.forName("UTF-16"));
        // THEN the existing XmlDeclaration should be updated in-place for encoding only, without adding 'version'
        org.jsoup.nodes.Node first = doc.childNode(0);
        assertTrue(first instanceof XmlDeclaration, "Expected XmlDeclaration at the first position after charset change");
        XmlDeclaration out = (XmlDeclaration) first;
        String expectedEnc = Charset.forName("UTF-16").displayName();
        assertEquals(expectedEnc, out.attr("encoding"), "XmlDeclaration encoding should be updated to the new charset");
        assertFalse(out.hasAttr("version"), "XmlDeclaration should not gain a version attribute if it was absent");
    }
}