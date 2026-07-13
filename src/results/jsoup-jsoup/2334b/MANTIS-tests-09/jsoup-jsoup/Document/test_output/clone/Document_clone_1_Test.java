package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for Document.clone() covering parser independence, connection copying, and attribute copying.
 */
public class Document_clone_1_Test {

    @Test
    @DisplayName("clone() produces a parser instance independent of the original when trackErrors is modified")
    public void test_TC04() {
        // GIVEN a new Document with default parser; no errors tracked initially
        Document original = new Document("http://test");
        // original.parser().getErrors().isEmpty() is precondition, ensures B0->B1->B2 path start
        assertTrue(original.parser().getErrors().isEmpty(), "Precondition: no track errors initially");

        // WHEN cloning and modifying clone's parser track errors => exercises Parser.clone branch (B3->B4->B5)
        Document clone = (Document) original.clone(); // Ensure clone method is defined in Document
        clone.parser().setTrackErrors(10);

        // THEN original remains at 0 and clone at 10, proving independent parser instances
        assertEquals(0, original.parser().getTrackErrors(), 
                "Original parser trackErrors should remain at default 0 after clone modification");
        assertEquals(10, clone.parser().getTrackErrors(), 
                "Clone parser trackErrors should be set to 10 independently");
    }

    @Test
    @DisplayName("clone() copies a non-null Connection so clone.connection() returns the same instance instead of a new session")
    public void test_TC05() {
        // GIVEN a Document with a custom Connection set (exercises connection != null branch B6)
        Document original = new Document("http://foo");
        Connection conn = Jsoup.connect("http://foo");
        original.connection(conn); // sets non-null connection field

        // WHEN cloning the document => super.clone should copy the connection field (B7)
        Document clone = (Document) original.clone(); // Ensure clone method is defined in Document

        // THEN clone.connection() returns the same instance, not a new session
        assertSame(conn, clone.connection(), 
                "Clone should retain the same Connection instance as original when non-null");
    }

    @Test
    @DisplayName("clone() copies attributes from the root element so an attribute added to original is present on the clone")
    public void test_TC06() {
        // GIVEN a Document with a custom attribute on the root element (ensures attribute branch B8)
        Document original = new Document("http://attr");
        original.attr("data-test", "value123");
        assertEquals("value123", original.attr("data-test"), 
                "Precondition: original has attribute data-test=value123");

        // WHEN cloning the document => Element.clone should copy attributes to new root element
        Document clone = (Document) original.clone(); // Ensure clone method is defined in Document

        // THEN clone has the same attribute key and value as original
        assertEquals("value123", clone.attr("data-test"), 
                "Clone should have copied the attribute data-test with value value123");
    }
}