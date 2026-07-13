package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for Document.charset(Charset) covering HTML and XML modes.
 */
public class Document_charset_1_Test {

    @Test
    @DisplayName("charset(Charset.US_ASCII) in HTML mode updates existing <meta charset> and removes mixed obsolete <meta name=charset>")
    public void test_TC07() {
        // GIVEN: a shell document with head containing one meta[charset] and two obsolete meta[name=charset]
        Document doc = Document.createShell("http://example.com");
        // Add existing valid charset meta to trigger update branch (B2):
        doc.head().appendElement("meta").attr("charset", "OLD");
        // Add obsolete name=charset metas to trigger removal (B3):
        doc.head().appendElement("meta").attr("name", "charset");
        doc.head().appendElement("meta").attr("name", "charset");

        // WHEN: setting charset to US-ASCII in HTML syntax (default)
        doc.charset(Charset.forName("US-ASCII"));

        // THEN: the existing meta[charset] should be updated, and obsolete metas removed
        String updated = doc.selectFirst("meta[charset]").attr("charset");
        assertEquals(Charset.forName("US-ASCII").displayName(), updated,
            // verify the update branch was taken and the attribute changed
            "Expected existing meta[charset] to be updated to US-ASCII"
        );
        Elements obsolete = doc.select("meta[name=charset]");
        assertTrue(obsolete.isEmpty(),
            // verify removal branch: no obsolete meta[name=charset] remains
            "Expected all meta[name=charset] elements to be removed"
        );
    }

    @Test
    @DisplayName("charset(Charset.UTF_16LE) in XML mode prepends new XmlDeclaration when first child is XmlDeclaration with non-xml name")
    public void test_TC08() {
        // GIVEN: a shell document in XML syntax with updateMetaCharset enabled and a non-xml declaration
        Document doc = Document.createShell("http://example.com");
        doc.updateMetaCharsetElement(true); // enable meta charset updates
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml); // set XML mode to follow xml branch (B1->B4)
        // Create a non-xml declaration as first child to hit B4
        XmlDeclaration fooDecl = new XmlDeclaration("foo", false);
        fooDecl.attr("version", "2.0");
        fooDecl.attr("encoding", "OLD");
        doc.prependChild(fooDecl);

        // WHEN: setting charset to UTF-16LE should prepend a new xml declaration before 'foo'
        doc.charset(Charset.forName("UTF-16LE"));

        // THEN: first child is the new xml declaration with version=1.0 and encoding=UTF-16LE
        Node first = doc.childNode(0);
        assertAll("Verify new XmlDeclaration prepended and attributes",
            () -> assertTrue(first instanceof XmlDeclaration,
                "Expected first child to be an XmlDeclaration"),
            () -> assertEquals("xml", ((XmlDeclaration) first).name(),
                "Expected declaration name to be 'xml'"),
            () -> assertEquals("1.0", ((XmlDeclaration) first).attr("version"),
                "Expected xml declaration version to default to '1.0'"),
            () -> assertEquals(Charset.forName("UTF-16LE").displayName(),
                ((XmlDeclaration) first).attr("encoding"),
                "Expected xml declaration encoding to be updated to 'UTF-16LE'"
            )
        );
    }
}