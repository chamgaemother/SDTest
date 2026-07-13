package org.jsoup.nodes;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for org.jsoup.nodes.Document.clone() method.  
 * Covers scenarios: TC03, TC04, TC05.  
 */
public class Document_clone_1_Test {

    @Test
    @DisplayName("clone() preserves child DocumentType node and its position")
    public void test_TC03() {
        // GIVEN: a Document with a DocumentType prepended, so clone() must find and copy first child
        Document orig = new Document("http://x");
        DocumentType dt = new DocumentType("html", "", "http://x");
        orig.prependChild(dt);
        // WHEN: cloning the document
        Document copy = orig.clone();
        // THEN: original and copy are distinct
        assertNotSame(orig, copy, "Clone should not return the same instance");
        // documentType() should return a distinct DocumentType
        DocumentType origDt = orig.documentType();
        DocumentType copyDt = copy.documentType();
        assertNotNull(origDt, "Original DocumentType should not be null");
        assertNotNull(copyDt, "Cloned DocumentType should not be null");
        assertNotSame(origDt, copyDt, "Cloned DocumentType should be a different instance");
        // And name preserved
        assertEquals(origDt.name(), copyDt.name(), "DocumentType name should be preserved in clone");
    }

    @Test
    @DisplayName("clone() duplicates element tree including head and body children")
    public void test_TC04() {
        // GIVEN: a shell document with html, head, body structure
        Document orig = Document.createShell("http://y");
        // WHEN: cloning the document deep including children
        Document copy = orig.clone();
        // THEN: head() and body() elements should be duplicated, not shared
        Element origHead = orig.head();
        Element copyHead = copy.head();
        assertNotSame(origHead, copyHead, "Head element should not be the same instance after clone");

        Element origBody = orig.body();
        Element copyBody = copy.body();
        assertNotSame(origBody, copyBody, "Body element should not be the same instance after clone");
    }

    @Test
    @DisplayName("clone() copies root attributes without sharing mutable map")
    public void test_TC05() {
        // GIVEN: a Document with an attribute on its root element (the #root node attributes)
        Document orig = new Document("http://z");
        orig.attr("lang", "en"); // set attribute
        // WHEN: cloning the document should copy attributes map deeply
        Document copy = orig.clone();
        // THEN: copied document retains initial attribute
        assertEquals("en", copy.attr("lang"), "Cloned document should have same 'lang' attribute value");
        // Modify original attribute map; clone should not be affected
        orig.attr("lang", "fr");
        assertEquals("en", copy.attr("lang"), "Changing original's attribute should not affect cloned document");
    }
}