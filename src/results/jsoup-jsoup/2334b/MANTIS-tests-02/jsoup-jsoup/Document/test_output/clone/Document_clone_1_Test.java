package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Document_clone_1_Test {

    @Test
    @DisplayName("TC11 clone() copies a non-null connection field so that clone.connection() returns the same Connection instance")
    public void test_TC11() {
        // GIVEN a Document with a custom Connection set
        Document doc = new Document("http://test");
        Connection conn = Jsoup.connect("http://example.com");
        doc.connection(conn);
        // WHEN cloning the document (shallow Object.clone should copy the connection field)
        Document cloned = doc.clone();
        // THEN the clone.connection() should return the same instance (verifies B2→B3 path)
        assertSame(conn, cloned.connection(), 
            "The cloned Document should reference the exact same Connection instance as the original");
    }

    @Test
    @DisplayName("TC12 clone() copies attributes on the Document root element via the attribute-copy loop")
    public void test_TC12() {
        // GIVEN a Document with one attribute on its root element
        Document doc = new Document("http://test");
        doc.attr("data-key", "data-val");
        // WHEN cloning the document (clone() should copy all attributes from the original's root element)
        Document cloned = doc.clone();
        // THEN the clone should have the same attribute value, and be a distinct object
        assertEquals("data-val", cloned.attr("data-key"), 
            "The cloned Document should have the same 'data-key' attribute value as the original");
        assertNotSame(doc, cloned, 
            "The cloned Document instance should not be the same object reference as the original");
    }
}