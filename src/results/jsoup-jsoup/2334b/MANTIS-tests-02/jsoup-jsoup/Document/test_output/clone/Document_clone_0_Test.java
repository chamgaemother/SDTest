package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;
public class Document_clone_0_Test {

    @Test
    @DisplayName("clone() on empty Document returns a distinct instance with no child nodes (loop-0)")
    void test_TC01() {
        // GIVEN an empty document with no children triggers zero-iteration of clone child copy loop
        Document doc = new Document("http://example.com");
        // WHEN
        Document result = doc.clone();
        // THEN
        assertNotSame(doc, result, "clone() should produce a distinct instance");
        assertEquals(0, result.childNodes().size(), "clone of empty doc should have no child nodes");
        assertEquals("http://example.com", result.baseUri(), "clone should preserve baseUri");
    }

    @Test
    @DisplayName("clone() on Document.createShell(baseUri) clones html, head, body structure (loop-N where N=3)")
    void test_TC02() {
        // GIVEN a shell document with exactly 3 children appended to the root (html, head, body)
        Document doc = Document.createShell("http://example.com");
        // WHEN
        Document result = doc.clone();
        // THEN
        assertEquals(4, result.childNodes().size(),
                "clone should preserve the root and html element, total child nodes = 4 (root's children)");
        Element html = result.child(0);
        // head and body are inside the html element
        assertEquals("head", html.child(0).tagName(), "first child of html should be head");
        assertEquals("body", html.child(1).tagName(), "second child of html should be body");
    }

    @Test
    @DisplayName("clone() on Document with nested elements deep clones two-level hierarchy (loop-N where N=2)")
    void test_TC03() {
        // GIVEN a two-level nested structure (a > b) triggers nested loop clone paths
        Document doc = new Document("http://x");
        Element a = doc.appendElement("a");
        a.appendElement("b");
        // WHEN
        Document result = doc.clone();
        // THEN
        assertEquals(1, result.select("a > b").size(),
                "deep clone should copy both levels so that 'a > b' exists once");
    }

    @Test
    @DisplayName("clone() preserves original OutputSettings instance state but clones the settings object")
    void test_TC04() {
        // GIVEN a document with modified outputSettings.indentAmount
        Document doc = new Document("u");
        doc.outputSettings().indentAmount(5);
        // WHEN
        Document result = doc.clone();
        // THEN
        assertEquals(5, result.outputSettings().indentAmount(),
                "clone should preserve indentAmount value");
        assertNotSame(doc.outputSettings(), result.outputSettings(),
                "clone should have a distinct OutputSettings instance");
    }

    @Test
    @DisplayName("clone() preserves original Parser settings but clones the parser instance")
    void test_TC05() {
        // GIVEN a document with a custom parser set
        Document doc = new Document("u");
        Parser p = Parser.xmlParser();
        doc.parser(p);
        // WHEN
        Document result = doc.clone();
        // THEN
        assertEquals(p.settings(), result.parser().settings(),
                "clone should preserve parser settings");
        assertNotSame(p, result.parser(),
                "clone should have a distinct Parser instance");
    }

    @Test
    @DisplayName("clone() on Document with attributes copies attributes to clone (loop-N where N=1)")
    void test_TC06() {
        // GIVEN a document where one element has an attribute (one iteration of attribute copy loop)
        Document doc = new Document("u");
        Element e = doc.appendElement("div").attr("key", "val");
        // WHEN
        Document result = doc.clone();
        // THEN
        Element clonedDiv = result.selectFirst("div");
        assertEquals("val", clonedDiv.attr("key"),
                "attribute 'key' should be copied to cloned div");
        assertNotSame(e, clonedDiv,
                "cloned element should be a distinct instance from original");
    }

    @Test
    @DisplayName("clone() modifications to original Document after clone do not affect the clone (branch-original-modify)")
    void test_TC07() {
        // GIVEN an empty document
        Document doc = new Document("u");
        Document result = doc.clone();
        // WHEN original is modified
        doc.appendElement("p");
        // THEN clone must remain unaffected
        assertTrue(result.select("p").isEmpty(),
                "modifying original after clone should not affect the clone");
    }

    @Test
    @DisplayName("clone() modifications to clone do not affect the original Document (branch-clone-modify)")
    void test_TC08() {
        // GIVEN an empty document
        Document doc = new Document("u");
        // WHEN clone is modified
        Document clone = doc.clone();
        clone.appendElement("span");
        // THEN original must remain unaffected
        assertTrue(doc.select("span").isEmpty(),
                "modifying clone should not affect the original document");
    }

    @Test
    @DisplayName("clone() returns object of type Document distinct from original (branch-type-check)")
    void test_TC09() {
        // GIVEN an empty document
        Document doc = new Document("u");
        // WHEN
        Document result = doc.clone();
        // THEN
        assertTrue(result instanceof Document, "clone result should be instance of Document");
        assertEquals(Document.class, result.getClass(), "clone result class should be exactly Document");
        assertNotSame(doc, result, "clone should not be the same instance as original");
    }

    @Test
    @DisplayName("clone() preserves QuirksMode field value in cloned Document (branch-field-preservation)")
    void test_TC10() {
        // GIVEN a document with non-default quirksMode
        Document doc = new Document("u");
        doc.quirksMode(Document.QuirksMode.limitedQuirks);
        // WHEN
        Document result = doc.clone();
        // THEN
        assertEquals(Document.QuirksMode.limitedQuirks, result.quirksMode(),
                "clone should preserve the quirksMode field value");
    }
}