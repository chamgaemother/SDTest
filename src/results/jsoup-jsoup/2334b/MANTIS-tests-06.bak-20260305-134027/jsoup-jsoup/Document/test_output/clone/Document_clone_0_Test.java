package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Document_clone_0_Test {

    @Test
    @DisplayName("TC01: Deep clone produces distinct Document instance with same base state")
    public void test_TC01() {
        // GIVEN an original Document with known baseUri and default quirksMode
        Document orig = Document.createShell("http://example.com");
        // WHEN we deep clone it
        Document copy = orig.clone(); // Fixed from deepClone() to clone()
        // THEN the clone must be a different instance…
        assertNotSame(orig, copy, "Clone should not be the same instance as original");
        // …but preserve the same location (baseUri)
        assertEquals(orig.location(), copy.location(), "Clone should have same location");
        assertEquals(orig.baseUri(), copy.baseUri(), "Clone should have same baseUri");
        // …and preserve the same quirksMode
        assertEquals(orig.quirksMode(), copy.quirksMode(), "Clone should have same quirks mode");
    }

    @Test
    @DisplayName("TC02: Deep clone clones OutputSettings so modifications do not affect original")
    public void test_TC02() {
        // GIVEN an original Document and its clone
        Document orig = Document.createShell("http://example.org");
        Document copy = orig.clone(); // Fixed from deepClone() to clone()
        // Ensure the two documents start with prettyPrint true
        assertTrue(orig.outputSettings().prettyPrint(), "Original should start with prettyPrint enabled");
        assertTrue(copy.outputSettings().prettyPrint(), "Copy should start with prettyPrint enabled");
        // WHEN we disable prettyPrint on the clone's output settings
        copy.outputSettings().prettyPrint(false);
        // THEN the original's outputSettings.prettyPrint() must remain true, proving deep copy
        assertTrue(orig.outputSettings().prettyPrint(),
                   "Modifying clone's OutputSettings should not affect original's OutputSettings");
    }

    @Test
    @DisplayName("TC03: Deep clone clones Parser so modifications do not affect original")
    public void test_TC03() {
        // GIVEN an original Document and its clone
        Document orig = Document.createShell("http://example.net");
        Parser origParser = orig.parser();
        // Record default trackErrors size for orig parser
        int defaultErrors = origParser.getTrackErrors().size();
        Document copy = orig.clone(); // Fixed from deepClone() to clone()
        // WHEN we add a track error count to the clone's parser
        copy.parser().setTrackErrors(1);
        // THEN the original parser's trackErrors list size must remain unchanged
        assertEquals(defaultErrors, origParser.getTrackErrors().size(),
                     "Original parser's trackErrors size should remain at default after modifying clone's parser");
    }

    @Test
    @DisplayName("TC04: Deep clone preserves Connection field when set non-null")
    public void test_TC04() {
        // GIVEN a Document with an explicitly set non-null Connection
        Connection conn = Jsoup.newSession();
        Document orig = Document.createShell("http://example.com").connection(conn);
        // Precondition: orig.connection() returns our conn
        assertSame(conn, orig.connection(), "Original connection() should return the set Connection instance");
        // WHEN we clone the document
        Document copy = orig.clone(); // Fixed from deepClone() to clone()
        // THEN the clone's connection() must return the same Connection instance
        assertSame(conn, copy.connection(),
                   "Clone should preserve the same Connection instance as original");
    }
}