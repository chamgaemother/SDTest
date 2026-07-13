package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for org.jsoup.nodes.Document#clone()
 */
public class Document_clone_2_Test {

    @Test
    @DisplayName("clone() returns a distinct Document with same baseUri and no child nodes")
    public void test_TC01() {
        // GIVEN an empty document
        Document original = new Document("http://example.com");
        // WHEN cloned
        Document clone = original.clone();
        // THEN the clone is a new instance, same baseUri, but no child nodes
        assertNotSame(original, clone);
        assertEquals(original.baseUri(), clone.baseUri());
        assertTrue(clone.childNodes().isEmpty());
    }

    @Test
    @DisplayName("clone() deep-copies default OutputSettings so changing original.charset() does not affect clone")
    public void test_TC02() {
        // GIVEN a new document with default UTF-8 outputSettings
        Document original = new Document("u");
        Charset iso = Charset.forName("ISO-8859-1");
        // WHEN cloned and original charset changed
        Document clone = original.clone();
        original.charset(iso);
        // THEN clone retains original charset UTF-8
        assertEquals(StandardCharsets.UTF_8, clone.charset());
    }

    @Test
    @DisplayName("clone() deep-copies Parser so errors in original.parser do not appear in clone.parser")
    public void test_TC03() {
        // GIVEN a document with parser tracking errors and having errors
        Document original = new Document("base");
        Parser p = original.parser();
        p.setTrackErrors(3);
        p.parseInput("<div><", original.baseUri());
        assertFalse(p.getErrors().isEmpty(), "Original parser should have errors");
        // WHEN cloned
        Document clone = original.clone();
        Parser cloneParser = clone.parser();
        // THEN parsers are distinct and clone has no errors
        assertNotSame(p, cloneParser);
        assertTrue(cloneParser.getErrors().isEmpty());
    }

    @Test
    @DisplayName("clone() leaves null connection so clone.connection() returns new session distinct from original")
    public void test_TC04() {
        // GIVEN a document with no explicit connection (null)
        Document original = new Document("u");
        // WHEN cloned
        Document clone = original.clone();
        Connection c1 = original.connection();
        Connection c2 = clone.connection();
        // THEN both connections are non-null and distinct sessions
        assertNotNull(c1);
        assertNotNull(c2);
        assertNotSame(c1, c2);
    }

    @Test
    @DisplayName("clone() preserves non-null connection so clone.connection() returns same instance")
    public void test_TC05() {
        // GIVEN a document with an explicit session connection
        Document original = new Document("http://x");
        Connection sess = Jsoup.newSession();
        original.connection(sess);
        // WHEN cloned
        Document clone = original.clone();
        // THEN clone.connection() returns the same session
        assertSame(sess, clone.connection());
    }

    @Test
    @DisplayName("clone() copies quirksMode so changes to original after clone do not affect clone")
    public void test_TC06() {
        // GIVEN a document with quirks mode set
        Document original = new Document("u");
        original.quirksMode(Document.QuirksMode.quirks);
        // WHEN cloned and original changed to limitedQuirks
        Document clone = original.clone();
        original.quirksMode(Document.QuirksMode.limitedQuirks);
        // THEN clone retains its original quirksMode
        assertEquals(Document.QuirksMode.quirks, clone.quirksMode());
    }

    @Test
    @DisplayName("clone() clones attributes so adding attr to original after clone does not appear in clone")
    public void test_TC07() {
        // GIVEN a shell document with an attribute 'a'
        Document original = Document.createShell("x");
        original.attr("a", "1");
        // WHEN cloned and original modified with new attr 'b'
        Document clone = original.clone();
        original.attr("b", "2");
        // THEN clone does not have attribute 'b' nor 'a' (attributes deep-copied before 'b' addition)
        assertFalse(clone.hasAttr("b"));
        // Also original still has 'a'
        assertTrue(original.hasAttr("a"));
    }

    @Test
    @DisplayName("clone() clones childNodes so modifications on clone do not affect original")
    public void test_TC08() {
        // GIVEN a shell document
        Document original = Document.createShell("b");
        // WHEN cloned and a div appended to clone
        Document clone = original.clone();
        clone.appendElement("div");
        // THEN original has no 'div' elements
        assertTrue(original.select("div").isEmpty());
    }

    @Test
    @DisplayName("clone() preserves outerHtml so serializations of original and clone match")
    public void test_TC09() {
        // GIVEN a shell document with body text 'hello'
        Document original = Document.createShell("u");
        original.body().text("hello");
        // WHEN cloned
        Document clone = original.clone();
        // THEN outerHtml matches and contains 'hello'
        assertEquals(original.outerHtml(), clone.outerHtml());
        assertTrue(clone.outerHtml().contains("hello"));
    }

    @Test
    @DisplayName("clone() preserves title so clone.title() remains even if original changed afterwards")
    public void test_TC10() {
        // GIVEN a shell document with title 'T'
        Document original = Document.createShell("x");
        original.title("T");
        // WHEN cloned and original title changed to 'U'
        Document clone = original.clone();
        original.title("U");
        // THEN clone retains its title 'T'
        assertEquals("T", clone.title());
    }

    @Test
    @DisplayName("clone() supports chaining: modifying clone.outputSettings().prettyPrint does not affect original")
    public void test_TC11() {
        // GIVEN a new document with default prettyPrint true
        Document original = new Document("u");
        assertTrue(original.outputSettings().prettyPrint());
        // WHEN cloned and clone's prettyPrint set to false
        Document clone = original.clone();
        clone.outputSettings().prettyPrint(false);
        // THEN original remains true and clone is false
        assertTrue(original.outputSettings().prettyPrint());
        assertFalse(clone.outputSettings().prettyPrint());
    }
}