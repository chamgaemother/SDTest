package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Document_clone_1_Test {

    @Test
    @DisplayName("clone() on new empty Document results in distinct outputSettings and parser")
    public void test_TC07() {
        // GIVEN a brand new, empty Document with a known baseUri
        Document doc = new Document("http://example.com");
        // WHEN we clone the document
        Document clone = doc.clone();
        // THEN the clone must be a distinct object
        assertNotSame(doc, clone, "clone() should return a different Document instance");
        // AND the baseUri should be carried over exactly
        assertEquals(doc.baseUri(), clone.baseUri(), "baseUri should be preserved in clone");
        // AND the OutputSettings should be deep‐cloned (distinct objects)
        assertNotSame(doc.outputSettings(), clone.outputSettings(),
                "outputSettings in the clone must not be the same instance as the original");
        // AND the Parser should be deep‐cloned (distinct objects)
        assertNotSame(doc.parser(), clone.parser(),
                "parser in the clone must not be the same instance as the original");
    }

    @Test
    @DisplayName("clone() preserves custom Parser.trackErrors and isolates subsequent parser changes")
    public void test_TC08() {
        // GIVEN a Document with a custom Parser that tracks 7 errors
        Parser p = Parser.xmlParser().setTrackErrors(7);
        Document doc = new Document("http://example.com").parser(p);
        // WHEN we clone the document
        Document clone = doc.clone();
        // THEN the clone's parser.trackErrors must equal the original setting (7)
        assertEquals(7, clone.parser().getTrackErrors(),
                "clone.parser().getTrackErrors() should match the original parser setting");
        // AND changing the clone's parser errors should not affect the original parser
        clone.parser().setTrackErrors(0);
        assertEquals(7, doc.parser().getTrackErrors(),
                "modifying the clone's parser trackErrors must not affect the original parser");
    }

    @Test
    @DisplayName("clone() copies non-null Connection so clone.connection() returns same reference branch")
    public void test_TC09() {
        // GIVEN a Document with a non-null Connection (session)
        Connection c = Jsoup.connect("http://host");
        Document doc = new Document("http://host").connection(c);
        // WHEN we clone the document
        Document clone = doc.clone();
        // THEN both doc and clone must return the same Connection instance reference
        Connection origConn = doc.getConnection(); // Changed from connection() to getConnection()
        Connection cloneConn = clone.getConnection(); // Changed from connection() to getConnection()
        assertSame(origConn, cloneConn,
                "clone.connection() should return the same Connection instance as the original");
        // AND modifying timeout on cloneConn must not change the original session timeout
        // (ensuring session isolation even with same reference)
        int origTimeout = origConn.timeout(0); // Changed to use timeout(0)
        cloneConn.timeout(origTimeout + 1234); // This line remains unchanged
        assertEquals(origTimeout, origConn.timeout(0),
                "changing timeout on the clone's connection must not affect the original connection");
    }
}