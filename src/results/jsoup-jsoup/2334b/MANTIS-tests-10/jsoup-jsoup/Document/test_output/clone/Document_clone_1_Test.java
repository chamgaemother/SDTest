package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.parser.Parser;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.nodes.Entities.EscapeMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;
public class Document_clone_1_Test {

    @Test
    @DisplayName("clone() on a document with XML output syntax clones OutputSettings.syntax=xml and its escapeMode change branch")
    public void test_TC02() {
        // GIVEN a document with XML syntax so ensure ensureMetaCharsetElement will take xml branch
        Document doc = Document.createShell("http://x");
        doc.outputSettings().syntax(Syntax.xml);
        // WHEN cloning the document
        Document cloned = doc.clone();
        // THEN cloned is distinct
        assertNotSame(doc, cloned, "Clone should produce a new Document instance");
        // and the output syntax and escape mode are preserved and xml branch applied
        assertEquals(Syntax.xml, cloned.outputSettings().syntax(),
                "Cloned outputSettings syntax should remain XML");
        assertEquals(EscapeMode.xhtml, cloned.outputSettings().escapeMode(),
                "Setting syntax to XML should set escapeMode to XHTML in the clone");
    }

    @Test
    @DisplayName("clone() on document with doctype node preserves and deep-copies the DocumentType child branch")
    public void test_TC03() {
        // GIVEN an original document with an explicit DocumentType as first child
        Document original = new Document("http://u");
        DocumentType dt = new DocumentType("html", "publicIdOrig", "systemId");
        original.prependChild(dt);
        // sanity check original has doctype
        assertNotNull(original.documentType(), "Original should have a DocumentType child");

        // WHEN cloning the document
        Document cloned = original.clone();
        // THEN the clone has its own DocumentType instance, not the same object
        DocumentType clonedDt = cloned.documentType();
        assertNotNull(clonedDt, "Cloned documentType should not be null");
        assertNotSame(dt, clonedDt, "DocumentType should be deep-copied, not the same instance");
        // modify cloned DocumentType's publicId and ensure original is unchanged → deep copy behavior
        clonedDt.attr("publicId", "changed");
        assertNotEquals(clonedDt.attr("publicId"), original.documentType().attr("publicId"),
                "Changing the clone's DocumentType should not affect the original");
    }

    @Test
    @DisplayName("clone() on document with custom parser settings clones parser via Parser.clone() branch")
    public void test_TC04() {
        // GIVEN a document with a custom parser having specific settings
        Parser baseParser = Parser.htmlParser();
        // use same settings object to simulate custom configuration
        Parser customParser = Parser.htmlParser().settings(baseParser.settings());
        Document original = Document.createShell("p").parser(customParser);
        // WHEN cloning the document
        Document cloned = original.clone();
        // THEN parsers should not be the same instance but have identical settings
        assertNotSame(original.parser(), cloned.parser(),
                "Parser instances should be distinct after clone (deep copy)");
        assertEquals(original.parser().settings(), cloned.parser().settings(),
                "Parser settings should be equal between original and clone");
    }
}