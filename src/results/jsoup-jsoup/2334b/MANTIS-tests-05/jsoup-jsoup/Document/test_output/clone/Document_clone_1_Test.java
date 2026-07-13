package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;
public class Document_clone_1_Test {

    @Test
    @DisplayName("TC03: clone() on a document with custom Parser settings yields a deep copy of parser without affecting original")
    public void test_TC03() {
        // GIVEN: a document with a custom parser configured to track errors
        Document doc = Document.createShell("http://example.com");
        Parser custom = Parser.htmlParser().setTrackErrors(5);
        doc.parser(custom);
        // WHEN: cloning the document
        Document cloned = doc.clone();
        // THEN: the parsers are distinct instances but have equal settings
        assertNotSame(doc.parser(), cloned.parser(), "Expected parser instances to differ for deep clone");
        assertEquals(doc.parser().getErrorsTracked(), cloned.parser().getErrorsTracked(),
            "Expected tracked error settings to be copied");
        // modify clone parser; original should remain unchanged
        cloned.parser().setTrackErrors(0);
        assertNotEquals(doc.parser().getErrorsTracked(), cloned.parser().getErrorsTracked(),
            "Modifying cloned parser should not affect original parser settings");
    }

    @Test
    @DisplayName("TC04: clone() on a document with modified OutputSettings (escapeMode and charset) yields isolated copy retaining settings")
    public void test_TC04() {
        // GIVEN: a document with custom output settings
        Document doc = Document.createShell("http://example.com");
        doc.outputSettings().escapeMode(Entities.EscapeMode.extended).charset("ISO-8859-1");
        // WHEN: cloning the document
        Document cloned = doc.clone();
        // THEN: outputSettings are distinct but have equal property values
        assertNotSame(doc.outputSettings(), cloned.outputSettings(),
            "Expected outputSettings instances to differ for deep clone");
        assertEquals(Entities.EscapeMode.extended, cloned.outputSettings().escapeMode(),
            "Expected escapeMode to be retained in cloned outputSettings");
        assertEquals(Charset.forName("ISO-8859-1"), cloned.outputSettings().charset(),
            "Expected charset to be retained in cloned outputSettings");
        // modify clone outputSettings; original should remain unchanged
        cloned.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
        assertNotEquals(doc.outputSettings().escapeMode(), cloned.outputSettings().escapeMode(),
            "Modifying cloned outputSettings should not affect original");
    }

    @Test
    @DisplayName("TC05: clone() on a document with nested child elements yields a deep copy of childNodes that can be altered independently")
    public void test_TC05() {
        // GIVEN: a document with a nested div under body
        Document doc = Document.createShell("http://example.com");
        Element body = doc.body();
        Element div = body.appendElement("div").attr("id", "orig");
        // WHEN: cloning the document
        Document cloned = doc.clone();
        // THEN: the childNodes size at the root is the same (shell has only html element)
        assertEquals(doc.childNodes().size(), cloned.childNodes().size(),
            "Expected same number of root child nodes for cloned document");
        // find the cloned div by id "orig"
        Element clonedDiv = cloned.body().selectFirst("div#orig");
        assertNotNull(clonedDiv, "Expected cloned div to exist with id='orig'");
        // modify cloned div id; original document should not reflect this change
        clonedDiv.attr("id", "changed");
        assertNull(doc.selectFirst("div#changed"),
            "Changing cloned child node should not affect the original document");
    }
}