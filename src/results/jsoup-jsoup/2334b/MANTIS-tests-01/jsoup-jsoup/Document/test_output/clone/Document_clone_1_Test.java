package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Document_clone_1_Test {

    @Test
    @DisplayName("clone() preserves the XML parser instance independently of original after modification")
    public void test_TC08() {
        // GIVEN an original document with its parser set to XML to force the xml branch during parser.clone()
        Document orig = new Document("http://x");
        orig.setParser(Parser.xmlParser()); // Changed from parser() to setParser()

        // WHEN cloning and then changing the original parser to HTML
        Document copy = orig.clone();
        orig.setParser(Parser.htmlParser()); // Changed from parser() to setParser()

        // THEN the copy must still have the XML parser, and the original has switched to HTML
        assertAll(
            () -> assertEquals("xml", copy.parser().getName(), // Changed from getParser() to parser()
                "Expected cloned document to retain its XML parser instance"),
            () -> assertEquals("html", orig.parser().getName(), // Changed from getParser() to parser()
                "Expected original document parser to be changed to HTML without affecting the clone"
            )
        );
    }

    @Test
    @DisplayName("clone() with null connection returns default newSession() without invoking original connection")
    public void test_TC09() {
        // GIVEN an original document with no connection set (connection is null by default)
        Document orig = new Document("http://x");

        // WHEN cloning
        Document copy = orig.clone();

        // THEN the clone's connection().newRequest().timeoutMillis() must match a fresh session's timeoutMillis()
        int cloneTimeout = copy.connection().newRequest().timeoutMillis();
        int defaultTimeout = Jsoup.newSession().newRequest().timeoutMillis();
        assertEquals(defaultTimeout, cloneTimeout,
            "Expected clone to use a new default Jsoup session when original connection is null");
    }

    @Test
    @DisplayName("clone() preserves a custom Connection independently of subsequent changes on original")
    public void test_TC10() {
        // GIVEN an original document with a custom Jsoup session connection
        Document orig = new Document("http://x");
        Connection custom = Jsoup.newSession();
        orig.connection(custom);

        // WHEN cloning and then resetting the original connection to a different session
        Document copy = orig.clone();
        orig.connection(Jsoup.connect("http://other"));

        // THEN the clone must keep the original custom connection,
        // and the original must no longer reference that connection
        assertAll(
            () -> assertSame(custom, copy.connection(),
                "Expected clone to preserve the custom Connection instance"),
            () -> assertNotSame(custom, orig.connection(),
                "Expected original document connection to change without affecting the clone"
            )
        );
    }

    @Test
    @DisplayName("clone() preserves quirksMode setting across clone boundary")
    public void test_TC11() {
        // GIVEN an original document with its quirksMode set to limitedQuirks to cover quirksMode field clone path
        Document orig = new Document("http://x");
        orig.quirksMode(Document.QuirksMode.limitedQuirks);

        // WHEN cloning
        Document copy = orig.clone();

        // THEN the clone must have the same QuirksMode as the original at clone time
        assertEquals(Document.QuirksMode.limitedQuirks, copy.quirksMode(), // Changed from getQuirksMode() to quirksMode()
            "Expected clone to preserve the original document's quirksMode setting");
    }
}