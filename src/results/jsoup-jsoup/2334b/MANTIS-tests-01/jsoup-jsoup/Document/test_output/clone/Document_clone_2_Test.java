package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Document_clone_2_Test {

    @Test
    @DisplayName("clone() with no original connection preserves null connection state so clone.connection() returns a fresh session")
    public void test_TC12() {
        // GIVEN: orig.connection is null by default in new Document
        Document orig = new Document("http://example.com");
        // Verify precondition: orig.connection() should return a new session, not null
        Connection origConn = orig.connection(); // Fixed method to connection()
        assertNotNull(origConn, "orig.connection() must provide a session even if null internally");

        // WHEN: clone the document
        Document copy = orig.clone(); // B0->super.clone()->B1->B2: runs clone and copies connection==null

        // THEN: copy.connection() should not be the same object as orig.connection()
        Connection copyConn = copy.connection(); // Fixed method to connection()
        assertNotSame(origConn, copyConn, "Cloned document must have its own session, not reuse original");
        // Both sessions should behave like fresh sessions, compare a default setting: timeoutMillis
        long expectedTimeout = Jsoup.newSession().newRequest().timeoutMillis();
        long actualTimeout = copyConn.newRequest().timeoutMillis();
        assertEquals(expectedTimeout, actualTimeout,
                "Cloned session must have default timeout matching newSession/session defaults");
    }

    @Test
    @DisplayName("clone() preserves XML parser instance and is unaffected by subsequent original parser changes")
    public void test_TC13() {
        // GIVEN: orig.parser is set to XML parser
        Document orig = new Document("http://example.com");
        orig.parser(Parser.xmlParser()); // set to xml, so on clone we branch to parser.clone()
        assertEquals("xml", orig.parser().getName(), "Precondition: original parser must be XML"); // Fixed method to parser()

        // WHEN: clone the document and then change original parser back to HTML
        Document copy = orig.clone(); // B0->super.clone()->B1->parser.clone(): copy.parser is clone of xmlParser
        orig.parser(Parser.htmlParser()); // mutate original after clone to ensure isolation

        // THEN: copy.parser().getName() remains "xml"
        assertEquals("xml", copy.parser().getName(),
                "Cloned document must retain XML parser independent of original changes"); // Fixed method to parser()
        // And original parser now is html
        assertEquals("html", orig.parser().getName(),
                "Original document parser change must be effective and not affect the clone"); // Fixed method to parser()
    }
}