package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Document.clone() covering:
 * - deep copy of outputSettings (syntax & prettyPrint)
 * - handling of null connection (returns new sessions)
 * - independent DOM structure cloning
 */
public class Document_clone_1_Test {

    @Test
    @DisplayName("clone() on document with HTML outputSettings retains html syntax and independent deep copy of settings")
    public void test_TC05() {
        // GIVEN a Document with HTML syntax and prettyPrint disabled
        Document orig = Document.createShell("http://example.com");
        orig.outputSettings().syntax(Document.OutputSettings.Syntax.html).prettyPrint(false);
        // ensure we are on branch for html syntax (OutputSettings.Syntax.html) and skip xml path
        assertEquals(Document.OutputSettings.Syntax.html, orig.outputSettings().syntax());
        assertFalse(orig.outputSettings().prettyPrint());

        // WHEN cloning
        Document copy = orig.clone();

        // THEN the clone should retain html syntax and prettyPrint=false
        assertEquals(Document.OutputSettings.Syntax.html, copy.outputSettings().syntax());
        assertFalse(copy.outputSettings().prettyPrint());

        // modify clone's syntax to xml, should not affect original (deep copy)
        copy.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        assertEquals(Document.OutputSettings.Syntax.html, orig.outputSettings().syntax(),
            "Original's syntax must stay html when clone is modified");
    }

    @Test
    @DisplayName("clone() preserves null connection field so connection() returns a new session each time")
    public void test_TC06() {
        // GIVEN a Document created without setting connection => connection field is null
        Document orig = Document.createShell("http://example.org");
        // WHEN cloning and retrieving connections
        Document copy = orig.clone();
        Connection origConn = orig.connection(); // branch: connection was null => Jsoup.newSession()
        Connection copyConn = copy.connection(); // same branch on clone

        // THEN both connections are non-null and distinct sessions
        assertNotNull(origConn, "Original connection() must return a session when field is null");
        assertNotNull(copyConn, "Cloned connection() must return a session when field is null");
        assertNotSame(origConn, copyConn, "Each call returns a fresh session instance");
    }

    @Test
    @DisplayName("clone() produces independent DOM: adding child to clone does not affect original childNodes")
    public void test_TC07() {
        // GIVEN a Document shell with one body element and no extra children
        Document orig = Document.createShell("http://test.com");
        int origCount = orig.body().children().size();
        assertEquals(0, origCount, "Initial body should have no children under shell head/body");

        // WHEN cloning and appending a new div to clone's body
        Document copy = orig.clone();
        copy.body().appendElement("div"); // branch: body existed so appendElement path

        // THEN original remains unchanged, clone has one additional child
        assertEquals(origCount, orig.body().children().size(),
            "Original body child count must remain unchanged after clone modification");
        assertEquals(origCount + 1, copy.body().children().size(),
            "Cloned body child count must increase by one after append");
    }
}