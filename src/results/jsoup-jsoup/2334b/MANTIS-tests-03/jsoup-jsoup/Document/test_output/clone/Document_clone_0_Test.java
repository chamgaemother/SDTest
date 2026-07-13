package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Document_clone_0_Test {

    @Test
    @DisplayName("clone() on an empty Document returns a deep copy with distinct instance and cloned outputSettings and parser")
    public void test_TC01() {
        // GIVEN: an empty Document with no child nodes
        Document original = new Document("http://example.com");
        // WHEN: cloning the document
        Document clone = original.clone();
        // THEN: verify clone is a distinct instance
        assertNotSame(original, clone, "Clone should not be the same instance as original");
        // location is final and should be copied over, equality check
        assertEquals(original.location(), clone.location(), "Clone should preserve the original location");
        // outputSettings should be cloned (deep copy), not the same object
        assertNotSame(original.outputSettings(), clone.outputSettings(), "OutputSettings should be a distinct object in the clone");
        // parser should be cloned (deep copy), not the same object
        assertNotSame(original.parser(), clone.parser(), "Parser should be a distinct object in the clone");
        // no child nodes on original, so clone should also have none
        assertTrue(clone.childNodes().isEmpty(), "Clone of empty document should have no child nodes");
    }

    @Test
    @DisplayName("clone() on a shell Document preserves child structure and produces distinct child instances")
    public void test_TC02() {
        // GIVEN: a shell Document with html, head, and body elements as child nodes
        Document original = Document.createShell("http://site");
        // precondition check: shell should have exactly one root child (html), which in turn has head and body
        assertFalse(original.childNodes().isEmpty(), "Original shell document should have child nodes");
        // WHEN: cloning the document
        Document clone = original.clone();
        // THEN: top-level child list sizes should match
        assertEquals(original.childNodes().size(), clone.childNodes().size(),
                "Clone should have the same number of top-level child nodes as original");
        // Each top-level child in clone must be a distinct instance
        for (int i = 0; i < original.childNodes().size(); i++) {
            assertNotSame(original.childNodes().get(i), clone.childNodes().get(i),
                    String.format("Child node at index %d should be a distinct instance", i));
        }
        // Also check specific html element references differ
        Element origHtml = original.select("html").first();
        Element cloneHtml = clone.select("html").first();
        assertNotNull(origHtml, "Original html element should exist");
        assertNotNull(cloneHtml, "Cloned html element should exist");
        assertNotSame(origHtml, cloneHtml, "The <html> element in clone should be a distinct instance from the original");
    }

    @Test
    @DisplayName("clone() produces isolation: modifications to clone do not affect original")
    public void test_TC03() {
        // GIVEN: a shell Document with a title set to "Title"
        Document original = Document.createShell("uri");
        original.title("Title");
        assertEquals("Title", original.title(), "Precondition: original title should be 'Title'");
        // WHEN: cloning and then modifying the clone's title to "New"
        Document clone = original.clone();
        clone.title("New");
        // THEN: ensure isolation - original remains unchanged, clone reflects new title
        assertEquals("Title", original.title(),
                "Original document's title should remain unchanged after modifying the clone");
        assertEquals("New", clone.title(),
                "Cloned document's title should be updated to the new value");
    }
}