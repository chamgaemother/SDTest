package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import static org.junit.jupiter.api.Assertions.*;
public class Document_clone_1_Test {

    @Test
    @DisplayName("clone() deep copies element attributes and modifications to clone do not affect original")
    public void test_TC05() {
        // GIVEN a Document with a custom attribute on the root element
        Document doc = new Document("http://test");
        doc.attr("data-key", "value1");
        // WHEN cloning and then modifying the clone's attribute
        Document copy = doc.clone(); // enters clone path B0→B1→B2→B4→B5 for attributes copy
        copy.attr("data-key", "value2"); // modifies clone only
        // THEN original remains unchanged, clone has new value
        assertEquals("value1", doc.attr("data-key"),
            "Original document's attribute should remain unchanged after clone modification");
        assertEquals("value2", copy.attr("data-key"),
            "Clone's attribute should reflect the modified value");
    }

    @Test
    @DisplayName("clone() does not copy a non-null connection; clone.connection() returns new session independent of original")
    public void test_TC06() {
        // GIVEN a Document with an explicit Connection set (non-null)
        Connection conn = Jsoup.connect("http://orig");
        Document doc = new Document("http://orig").connection(conn);
        // WHEN cloning the document
        // This exercises path B0→B1→B3→B6: clone should preserve original.connection field on original only
        Document copy = doc.clone();
        Connection copyConn = copy.connection(); // returns new session because copy.connection is null
        // THEN original.connection() remains the same instance
        assertSame(conn, doc.connection(),
            "Original document should retain its originally set connection instance");
        // AND clone.connection() is a new, non-null, independent session
        assertNotSame(conn, copyConn,
            "Cloned document should not share the same Connection instance as original");
        assertNotNull(copyConn,
            "Cloned document's connection() should never return null");
    }

    @Test
    @DisplayName("clone() copies and isolates quirksMode; changing clone quirksMode does not affect original")
    public void test_TC07() {
        // GIVEN a Document with quirksMode set to quirks
        Document doc = new Document("http://q").quirksMode(Document.QuirksMode.quirks);
        // WHEN cloning the document
        // This follows path B0→B1→B2→B7 for quirksMode copy
        Document copy = doc.clone();
        // Modify clone's quirksMode to limitedQuirks
        copy.quirksMode(Document.QuirksMode.limitedQuirks);
        // THEN original quirksMode remains quirks
        assertEquals(Document.QuirksMode.quirks, doc.quirksMode(),
            "Original document's quirksMode should remain unchanged after clone modification");
        // AND clone's quirksMode reflects the new setting
        assertEquals(Document.QuirksMode.limitedQuirks, copy.quirksMode(),
            "Cloned document's quirksMode should reflect the newly set value");
    }
}