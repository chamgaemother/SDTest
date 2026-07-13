package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Document_clone_0_Test {

    @Test
    @DisplayName("clone() returns a distinct Document with identical empty content and baseUri")
    public void test_TC01() {
        // GIVEN an empty document with baseUri
        Document orig = new Document("http://example.com");
        // WHEN cloning the document (no children path B0->B1->B2->B3)
        Document copy = orig.clone();
        // THEN the clone is a different instance
        assertNotSame(orig, copy, "Clone should produce a new Document instance");
        // AND outerHtml and baseUri are identical
        assertEquals(orig.outerHtml(), copy.outerHtml(), "Clone should have identical empty content");
        assertEquals(orig.baseUri(), copy.baseUri(), "Clone should preserve baseUri");
    }

    @Test
    @DisplayName("clone() deep clones a document with a single child element and preserves mutation isolation")
    public void test_TC02() {
        // GIVEN a document with one div child and text (traverse child branch)
        Document orig = new Document("http://example.com");
        orig.appendChild(orig.createElement("div").text("text"));
        // WHEN cloning and mutating the clone
        Document copy = orig.clone();
        copy.appendChild(copy.createElement("span")); // mutation on clone
        // THEN clone should reflect new child
        assertTrue(copy.outerHtml().contains("<span>"), "Clone should contain the new span element");
        // AND original should not be affected
        assertFalse(orig.outerHtml().contains("<span>"), "Original should not be modified by clone changes");
    }

    @Test
    @DisplayName("clone() deep clones a document with multiple nested children and preserves nested structure")
    public void test_TC03() {
        // GIVEN a document with nested div>p structure (nested branch)
        Document orig = new Document("http://example.com");
        Element div = orig.appendChild(orig.createElement("div"));
        div.appendChild(orig.createElement("p").text("para"));
        // WHEN cloning and modifying nested p in clone
        Document copy = orig.clone();
        copy.selectFirst("p").text("changed"); // mutate nested child in clone
        // THEN clone p text updated
        assertEquals("changed", copy.selectFirst("p").text(), "Clone's nested p should be updated");
        // AND original remains with original text
        assertEquals("para", orig.selectFirst("p").text(), "Original's nested p should remain unchanged");
    }

    @Test
    @DisplayName("clone() copies outputSettings object by value so modifying original.outputSettings after clone does not affect clone")
    public void test_TC04() {
        // GIVEN a document with custom outputSettings (state branch)
        Document orig = new Document("http://x");
        orig.outputSettings().indentAmount(5);
        // WHEN cloning and then changing original's setting
        Document copy = orig.clone();
        orig.outputSettings().indentAmount(2); // mutate original's settings
        // THEN clone retains its prior indentAmount value
        assertEquals(5, copy.outputSettings().indentAmount(), "Clone's indentAmount should not change when original's is modified");
    }

    @Test
    @DisplayName("clone() copies parser object by value so modifying original.parser after clone does not affect clone.parser")
    public void test_TC05() {
        // GIVEN a document with XML parser set (parser branch)
        Document orig = new Document("http://x");
        orig.parser(Parser.xmlParser());
        // WHEN cloning and then resetting original to HTML parser
        Document copy = orig.clone();
        orig.parser(Parser.htmlParser());
        // THEN clone's parser remains XML
        assertEquals("xml", copy.parser().getName(), "Clone should preserve the XML parser after original is reset");
    }

    @Test
    @DisplayName("clone() preserves title element text in cloned document")
    public void test_TC06() {
        // GIVEN a document with a title set (head branch creates head/title)
        Document orig = new Document("http://x");
        orig.title("My Title");
        // WHEN cloning
        Document copy = orig.clone();
        // THEN clone.title() matches original
        assertEquals("My Title", copy.title(), "Clone should preserve the title text");
    }

    @Test
    @DisplayName("clone() preserves form elements and form-specific data in cloned document")
    public void test_TC07() {
        // GIVEN a document with a form element in body
        Document orig = new Document("http://x");
        org.jsoup.nodes.FormElement form = new org.jsoup.nodes.FormElement(org.jsoup.parser.Tag.valueOf("form"), "http://x", null);
        orig.appendChild(form);
        // WHEN cloning
        Document copy = orig.clone();
        // THEN clone.forms() size equals original's
        assertEquals(1, copy.forms().size(), "Clone should contain the same number of forms as original");
    }

    @Test
    @DisplayName("clone() preserves quirksMode in the cloned document")
    public void test_TC08() {
        // GIVEN a document with quirksMode set to limitedQuirks (state branch)
        Document orig = new Document("http://x");
        orig.setQuirksMode(Document.QuirksMode.limitedQuirks);
        // WHEN cloning
        Document copy = orig.clone();
        // THEN clone retains same quirksMode
        assertEquals(Document.QuirksMode.limitedQuirks, copy.quirksMode(), "Clone should preserve quirksMode");
    }

    @Test
    @DisplayName("clone() preserves connection field so that default Jsoup.newSession() is not invoked on clone without prior connection")
    public void test_TC09() {
        // GIVEN a document with no explicit connection (connection null branch)
        Document orig = new Document("http://x");
        // WHEN cloning
        Document copy = orig.clone();
        // THEN clone.connection() returns default session equivalent to Jsoup.newSession()
        long defaultTimeout = Jsoup.newSession().newRequest().timeoutMillis();
        long cloneTimeout = copy.connection().newRequest().timeoutMillis();
        assertEquals(defaultTimeout, cloneTimeout, "Clone should use default session settings when no connection was set");
    }

    @Test
    @DisplayName("clone() preserves explicitly set connection field and isolates subsequent changes")
    public void test_TC10() {
        // GIVEN a document with a custom connection (connection branch)
        Document orig = new Document("http://x");
        Connection conn = Jsoup.newSession();
        orig.setConnection(conn);
        // WHEN cloning and then changing original's connection
        Document copy = orig.clone();
        orig.setConnection(Jsoup.connect("http://other"));
        // THEN clone.connection() remains the originally set connection
        assertSame(conn, copy.connection(), "Clone should preserve the original Connection and not be affected by subsequent changes to original");
    }
}