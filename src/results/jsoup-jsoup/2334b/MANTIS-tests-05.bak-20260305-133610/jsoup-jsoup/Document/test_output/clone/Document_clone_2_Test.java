package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Document.clone()
 */
public class Document_clone_2_Test {

    @Test
    @DisplayName("TC11 clone() preserves and deep-copies document-level attributes")
    public void test_TC11() {
        // GIVEN: original Document with a custom attribute on the root (#document)
        Document original = new Document("http://base");
        original.attr("data-test", "123");
        // WHEN: clone is created via public clone(), exercising super.clone path
        Document clone = original.clone();
        // THEN: the clone has the same attribute value
        assertEquals("123", clone.attr("data-test"),
            "Cloned document should preserve attribute values from original");
        // AND: modifying clone's attribute does not affect the original (deep-copy of attributes)
        clone.attr("data-test", "456");
        assertEquals("123", original.attr("data-test"),
            "Modifying clone's attributes must not impact the original document's attributes");
    }

    @Test
    @DisplayName("TC12 clone() deep-copies a Document with a DocumentType child node")
    public void test_TC12() {
        // GIVEN: original Document with a DocumentType as first child
        Document original = new Document("http://base");
        DocumentType dt = new DocumentType("html", "pubid", "sysid");
        original.prependChild(dt);
        // WHEN: clone is created to deep-copy child nodes
        Document clone = original.clone();
        // THEN: the first child of the clone is a distinct DocumentType instance
        Node clonedFirst = clone.childNode(0);
        assertTrue(clonedFirst instanceof DocumentType,
            "The cloned first child must be a DocumentType, covering branch where instanceof matches");
        assertNotSame(dt, clonedFirst,
            "The cloned DocumentType must not be the same instance as the original (deep clone)");
        // AND: modifying the cloned DocumentType does not affect the original DocumentType node
        ((DocumentType) clonedFirst).attr("some", "value");
        assertNull(dt.attr("some"),
            "Mutations on the cloned DocumentType should not affect the original DocumentType instance");
    }
}