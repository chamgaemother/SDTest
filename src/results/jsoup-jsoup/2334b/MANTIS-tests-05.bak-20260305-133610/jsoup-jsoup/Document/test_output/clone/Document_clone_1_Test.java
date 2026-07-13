package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import static org.junit.jupiter.api.Assertions.*;
public class Document_clone_1_Test {

    @Test
    @DisplayName("clone() on empty Document produces distinct deep copy with no child nodes")
    public void test_TC06() {
        // GIVEN an empty document with no children (ensures branch: no child handling)
        Document original = new Document("http://base");
        
        // WHEN cloning the document (invokes public clone)
        Document clone = original.clone();
        
        // THEN clone is a different instance
        assertNotSame(original, clone);
        // and clone has no child nodes
        assertEquals(0, clone.childNodeSize());
        
        // and modifying original after clone does not affect clone (verifies deep copy)
        original.appendElement("div");
        assertEquals(0, clone.childNodeSize());
    }

    @Test
    @DisplayName("clone() copies nested element structure fully and deep, preserving text")
    public void test_TC07() {
        // GIVEN a document with nested <ul><li> structure and text (exercises loop over children)
        Document original = new Document("http://base");
        original.appendElement("ul").appendElement("li").text("item1");
        original.appendElement("li").text("item2");
        
        // WHEN cloning the document
        Document clone = original.clone();
        
        // THEN clone has same number of top-level children
        assertEquals(original.childNodeSize(), clone.childNodeSize());
        // and clone retains text of <li> elements
        assertEquals("item1 item2", clone.select("li").text());
        
        // and modifying clone does not impact original (deep independence)
        clone.selectFirst("li").text("changed");
        assertEquals("item1", original.selectFirst("li").text());
    }

    @Test
    @DisplayName("clone() retains null connection field leading to new session on clone.connection() branch")
    public void test_TC08() {
        // GIVEN a document with no connection set (connection == null triggers new session on access)
        Document original = new Document("http://base");
        
        // WHEN cloning and retrieving connections twice
        Document clone = original.clone();
        Connection c1 = clone.connection(); // Changed to use the public getter method
        Connection c2 = clone.connection(); // Changed to use the public getter method
        
        // THEN each call returns a non-null, distinct session
        assertNotNull(c1);
        assertNotNull(c2);
        assertNotSame(c1, c2);
    }

    @Test
    @DisplayName("clone() copies non-null connection field so clone.connection() returns same session object")
    public void test_TC09() {
        // GIVEN a document with an explicit connection set (bypass null branch)
        Document original = new Document("http://base");
        Connection custom = Jsoup.connect("http://example.com").newRequest();
        original.connection(custom);
        
        // WHEN cloning the document
        Document clone = original.clone();
        
        // THEN clone.connection returns the same instance as original
        assertSame(custom, clone.connection()); // Changed to use the public getter method
    }

    @Test
    @DisplayName("clone() copies outputSettings, parser, and quirksMode independently")
    public void test_TC10() {
        // GIVEN a document with custom parser, outputSettings, and quirksMode
        Document original = new Document("http://base");
        original.parser(Parser.xmlParser());
        original.outputSettings().prettyPrint(false).outline(true);
        original.quirksMode(Document.QuirksMode.limitedQuirks);
        
        // WHEN cloning the document
        Document clone = original.clone();
        
        // THEN initial clones match original settings (ensures field copy)
        assertEquals(original.parser().getSettings(), clone.parser().getSettings());
        assertEquals(original.outputSettings().prettyPrint(), clone.outputSettings().prettyPrint());
        assertEquals(original.outputSettings().outline(), clone.outputSettings().outline());
        assertEquals(Document.QuirksMode.limitedQuirks, clone.quirksMode());
        
        // and mutate clone's fields to different values (ensures independent copies)
        clone.parser(Parser.htmlParser());
        clone.outputSettings().prettyPrint(true).outline(false);
        clone.quirksMode(Document.QuirksMode.quirks);
        
        // THEN original remains unchanged after clone modifications
        assertEquals(Parser.xmlParser().getSettings(), original.parser().getSettings());
        assertFalse(original.outputSettings().prettyPrint());
        assertTrue(original.outputSettings().outline());
        assertEquals(Document.QuirksMode.limitedQuirks, original.quirksMode());
    }
}