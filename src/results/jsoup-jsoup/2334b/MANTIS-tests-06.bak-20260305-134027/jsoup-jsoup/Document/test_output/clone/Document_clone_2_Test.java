package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.internal.StringUtil;
import org.jsoup.parser.Parser;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Document_clone_2_Test {

    @Test
    @DisplayName("TC08: clone() deep-copies OutputSettings.escapeMode so modifying clone’s escapeMode does not affect original")
    public void test_TC08() {
        // GIVEN: a shell document with escapeMode set to EXTENDED
        Document orig = Document.createShell("http://example.com");
        orig.outputSettings().escapeMode(EscapeMode.extended);
        // WHEN: we clone and then change the clone's escapeMode to BASE
        Document copy = orig.clone();
        // change clone's escapeMode: exercises OutputSettings.clone path
        copy.outputSettings().escapeMode(EscapeMode.base);
        // THEN: original remains extended, clone is base
        assertAll(
            () -> assertEquals(EscapeMode.extended, orig.outputSettings().escapeMode(),
                    "Original must keep its extended escapeMode"),
            () -> assertEquals(EscapeMode.base, copy.outputSettings().escapeMode(),
                    "Clone must reflect its own base escapeMode")
        );
    }

    @Test
    @DisplayName("TC09: clone() deep-copies indentAmount so modifying clone’s indent does not affect original")
    public void test_TC09() {
        // GIVEN: a shell document with indentAmount set to 5
        Document orig = Document.createShell("http://example.org");
        orig.outputSettings().indentAmount(5);
        // WHEN: we clone and then change clone's indentAmount to 2
        Document copy = orig.clone();
        // change clone's indent: covers OutputSettings.clone path for indentAmount field
        copy.outputSettings().indentAmount(2);
        // THEN: original stays at 5, clone is 2
        assertAll(
            () -> assertEquals(5, orig.outputSettings().indentAmount(),
                    "Original must keep indentAmount 5"),
            () -> assertEquals(2, copy.outputSettings().indentAmount(),
                    "Clone must have its own indentAmount 2")
        );
    }

    @Test
    @DisplayName("TC10: clone() preserves element attributes and cloning is independent")
    public void test_TC10() {
        // GIVEN: a shell document, and set attribute on its <html> element
        Document orig = Document.createShell("http://test.com");
        Element htmlOrig = orig.select("html").first(); // Changed to select method
        htmlOrig.attr("data-test", "origVal");
        // WHEN: clone and modify clone's html attribute
        Document copy = orig.clone();
        Element htmlCopy = copy.select("html").first(); // Changed to select method
        htmlCopy.attr("data-test", "copyVal");
        // THEN: original's attribute remains origVal, clone's is copyVal
        assertAll(
            () -> assertEquals("origVal", orig.select("html").first().attr("data-test"),
                    "Original html element attribute unchanged"),
            () -> assertEquals("copyVal", copy.select("html").first().attr("data-test"),
                    "Clone html element attribute updated independently")
        );
    }

    @Test
    @DisplayName("TC11: clone() preserves DocumentType child and clones it independently")
    public void test_TC11() {
        // GIVEN: a shell document and we prepend a DocumentType node
        Document orig = Document.createShell("http://site");
        org.jsoup.nodes.DocumentType dt = new org.jsoup.nodes.DocumentType("html", "-//W3C//DTD HTML 4.01//EN", ""); // Updated import
        orig.prependChild(dt);
        // WHEN: clone the document (executes super.clone and child clone)
        Document copy = orig.clone();
        // THEN: both have a non-null DocumentType, but different instances with same name
        org.jsoup.nodes.DocumentType origDt = (org.jsoup.nodes.DocumentType) orig.childNode(0); // Updated to use childNode
        org.jsoup.nodes.DocumentType copyDt = (org.jsoup.nodes.DocumentType) copy.childNode(0); // Updated to use childNode
        assertAll(
            () -> assertNotNull(origDt, "Original must have a DocumentType"),
            () -> assertNotNull(copyDt, "Clone must have a DocumentType"),
            () -> assertNotSame(origDt, copyDt, "DocumentType must be a distinct instance"),
            () -> assertEquals(origDt.name(), copyDt.name(),
                    "Both DocumentTypes must share the same name")
        );
    }

    @Test
    @DisplayName("TC12: clone() on Document with XML parser settings ensures parser is deep-cloned and independent")
    public void test_TC12() {
        // GIVEN: an XML parser and document set to use it; record original trackErrors size
        Parser xmlParser = Parser.xmlParser();
        Document orig = Document.createShell("http://xml").parser(xmlParser);
        int origSize = orig.parser().getTrackErrors().size();
        // WHEN: clone and then set clone's trackErrors capacity to 2
        Document copy = orig.clone();
        copy.parser().setTrackErrors(2);
        // THEN: original parser errors list size unchanged, clone's size is 2
        assertAll(
            () -> assertEquals(origSize, orig.parser().getTrackErrors().size(),
                    "Original parser's trackErrors size must remain unchanged"),
            () -> assertEquals(2, copy.parser().getTrackErrors().size(),
                    "Clone parser's trackErrors size must reflect the new capacity 2")
        );
    }
}