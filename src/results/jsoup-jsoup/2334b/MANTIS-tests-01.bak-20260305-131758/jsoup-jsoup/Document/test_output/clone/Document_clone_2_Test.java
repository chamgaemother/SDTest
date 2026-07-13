package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.QuirksMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Document_clone_2_Test {

    @Test
    @DisplayName("clone() on default Document returns new session on connection() null branch")
    public void test_TC06() {
        // GIVEN: Document with no connection set (connection field is null)
        Document orig = new Document("http://ex");
        
        // WHEN: clone the document and retrieve connections
        Document copy = orig.clone();
        Connection c1 = orig.connection();  // triggers null branch: returns new session
        Connection c2 = copy.connection();  // triggers null branch on clone as well
        
        // THEN: both sessions should be non-null and distinct instances
        assertNotNull(c1, "Original connection() should return a non-null session when connection field is null");
        assertNotNull(c2, "Cloned connection() should return a non-null session when connection field is null");
        assertNotSame(c1, c2, "Original and cloned sessions must be distinct instances to avoid shared state");
    }

    @Test
    @DisplayName("clone() preserves non-null Connection field branch in connection()")
    public void test_TC07() {
        // GIVEN: Document with a custom non-null connection set
        Document orig = new Document("http://ex");
        Connection custom = Jsoup.newSession();
        orig.connection(custom); // set non-null connection to trigger preserved branch
        
        // WHEN: clone the document and retrieve its connection
        Document copy = orig.clone();
        Connection c = copy.connection(); // should return the same custom instance
        
        // THEN: the clone should preserve the exact connection instance
        assertSame(custom, c, "Cloned document should preserve the same non-null Connection instance set on original");
    }

    @Test
    @DisplayName("clone() copies non-default QuirksMode branch")
    public void test_TC08() {
        // GIVEN: Document with a custom quirksMode to test branch Q1
        Document orig = new Document("http://ex");
        orig.quirksMode(QuirksMode.limitedQuirks);
        
        // WHEN: clone the document
        Document copy = orig.clone();
        
        // THEN: the cloned document retains the same quirksMode and is a distinct object
        assertEquals(QuirksMode.limitedQuirks, copy.quirksMode(),
            "Cloned document should have the same QuirksMode as original");
        assertNotSame(orig, copy, "Original and clone must be different Document instances");
    }

    @Test
    @DisplayName("clone() deep-clones nested element tree without shared mutability")
    public void test_TC09() {
        // GIVEN: a shell document with nested div > span set to "hello"
        Document orig = Document.createShell("http://ex");
        // create nested elements under body: <div><span>hello</span></div>
        orig.body().appendElement("div").appendElement("span").text("hello");
        
        // WHEN: clone original and then mutate original's nested span text
        Document copy = orig.clone();
        orig.body().selectFirst("div > span").text("changed"); // mutate original after clone
        
        // THEN: clone's nested span text remains as "hello", proving deep copy
        String copyText = copy.body().selectFirst("div > span").text();
        assertEquals("hello", copyText, "Cloned nested span text should remain unchanged after original mutation");
    }
}