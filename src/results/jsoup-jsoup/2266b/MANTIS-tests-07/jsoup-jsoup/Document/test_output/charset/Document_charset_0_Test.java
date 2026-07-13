package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
public class Document_charset_0_Test {

    @Test
    @DisplayName("TC01: charset(Charset) with no existing <meta charset> and default html syntax creates a new meta charset element")
    public void test_TC01() {
        // GIVEN: a fresh HTML document shell has head but no meta[charset]; updateMetaCharset is default (false)
        Document doc = Document.createShell("http://example.com");
        Charset cs = StandardCharsets.ISO_8859_1;
        // Sanity: no existing meta[charset]
        assertNull(doc.head().selectFirst("meta[charset]"));

        // WHEN: call charset, which should set updateMetaCharset=true, assign outputSettings.charset and add meta[charset]
        doc.charset(cs);

        // THEN: updateMetaCharset flag flipped true
        assertTrue(doc.updateMetaCharsetElement(), "updateMetaCharsetElement should be true after charset()");
        // outputSettings charset should match
        assertEquals(cs, doc.charset(), "Document.charset() should return the provided charset");
        // new meta[charset] element added with correct value
        Elements metas = doc.head().select("meta[charset]");
        assertEquals(1, metas.size(), "One meta[charset] element should be present");
        assertEquals(cs.displayName(), metas.get(0).attr("charset"), "meta[charset] attr must equal charset.displayName()");
        // obsolete meta[name=charset] should not exist
        assertTrue(doc.head().select("meta[name=charset]").isEmpty(), "No obsolete meta[name=charset] elements must remain");
    }

    @Test
    @DisplayName("TC02: charset(Charset) with existing <meta charset> and default html syntax updates existing element")
    public void test_TC02() {
        // GIVEN: HTML shell with one existing meta[charset="UTF-8"]
        Document doc = Document.createShell("http://example.com");
        doc.head().appendElement("meta").attr("charset", "UTF-8");
        Charset cs = StandardCharsets.US_ASCII;
        // Precondition: exactly one meta[charset]
        assertEquals(1, doc.head().select("meta[charset]").size());

        // WHEN: call charset to update existing meta
        doc.charset(cs);

        // THEN: updateMetaCharset flipped true
        assertTrue(doc.updateMetaCharsetElement(), "updateMetaCharsetElement should be true after charset()");
        // Document charset updated
        assertEquals(cs, doc.charset(), "Document.charset() should return the provided charset");
        // Existing meta[charset] updated rather than duplicated
        Elements metas = doc.head().select("meta[charset]");
        assertEquals(1, metas.size(), "Should still have one meta[charset] element");
        assertEquals(cs.displayName(), metas.get(0).attr("charset"), "meta[charset] attr must be updated to new value");
    }

    @Test
    @DisplayName("TC03: charset(Charset) with xml syntax and no existing XmlDeclaration prepends a new xml declaration")
    public void test_TC03() {
        // GIVEN: new Document with xml syntax, updateMetaCharsetElement initially false, no XmlDeclaration child
        Document doc = new Document("http://example.com");
        doc.updateMetaCharsetElement(false);
        doc.outputSettings().syntax(OutputSettings.Syntax.xml);
        Charset cs = StandardCharsets.UTF_16;
        // Precondition: no XmlDeclaration at index 0
        assertTrue(doc.childNodes().isEmpty() || !(doc.childNodes().get(0) instanceof XmlDeclaration));

        // WHEN: call charset, which should set updateMetaCharset=true and prepend XmlDeclaration
        doc.charset(cs);

        // THEN: updateMetaCharset toggled true
        assertTrue(doc.updateMetaCharsetElement(), "updateMetaCharsetElement should be true after charset()");
        // Document charset updated
        assertEquals(cs, doc.charset(), "Document.charset() should return the provided charset");
        // First child must be XmlDeclaration with name 'xml'
        Node first = doc.childNodes().get(0);
        assertTrue(first instanceof XmlDeclaration, "First child should be XmlDeclaration");
        XmlDeclaration decl = (XmlDeclaration) first;
        assertEquals("xml", decl.name(), "XmlDeclaration name must be 'xml'");
        assertEquals(cs.displayName(), decl.attr("encoding"), "XmlDeclaration encoding must match charset.displayName()");
        assertEquals("1.0", decl.attr("version"), "XmlDeclaration version must be '1.0'");
    }

    @Test
    @DisplayName("TC04: charset(Charset) with xml syntax and existing xml declaration updates attributes when name is xml")
    public void test_TC04() {
        // GIVEN: Document with existing XmlDeclaration name='xml', version='0.5', encoding='X'; xml syntax
        Document doc = new Document("http://ex");
        XmlDeclaration existing = new XmlDeclaration("xml", false);
        existing.attr("version", "0.5");
        existing.attr("encoding", "X");
        doc.prependChild(existing);
        doc.outputSettings().syntax(OutputSettings.Syntax.xml);
        Charset cs = StandardCharsets.UTF_8;
        // Precondition: first child is the existing declaration
        assertSame(existing, doc.childNodes().get(0), "Precondition: existing declaration must be first");

        // WHEN: call charset to update existing XmlDeclaration
        doc.charset(cs);

        // THEN: updateMetaCharset toggled true
        assertTrue(doc.updateMetaCharsetElement(), "updateMetaCharsetElement should be true after charset()");
        // Document charset updated
        assertEquals(cs, doc.charset(), "Document.charset() should return the provided charset");
        // The same XmlDeclaration instance is updated
        Node first = doc.childNodes().get(0);
        assertSame(existing, first, "XmlDeclaration instance should be same object after update");
        XmlDeclaration decl = (XmlDeclaration) first;
        assertEquals(cs.displayName(), decl.attr("encoding"), "XmlDeclaration encoding must match charset.displayName()");
        assertEquals("1.0", decl.attr("version"), "XmlDeclaration version should be reset to '1.0'");
    }
}