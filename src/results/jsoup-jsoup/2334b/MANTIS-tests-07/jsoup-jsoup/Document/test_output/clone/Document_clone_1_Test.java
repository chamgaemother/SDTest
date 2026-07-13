package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Document_clone_1_Test {

    @Test
    @DisplayName("clone() produces a deep copy: modifying clone's child elements does not affect the original")
    public void test_TC03() {
        // GIVEN a document with an appended header element inside body
        Document original = Document.createShell("http://example.com");
        // body() should create html/body structure; then append header
        Element header = original.body().appendElement("header");
        assertNotNull(header, "Precondition failed: header should be appended");

        // WHEN clone() is called to produce a deep copy
        Document copy = original.clone();
        // Modifications to the clone's body: add footer, remove header
        copy.body().appendElement("footer");
        copy.body().selectFirst("header").remove();

        // THEN original and copy are distinct objects
        assertNotSame(original, copy, "Clone should not be the same instance as original");
        // original still has its header (clone removed header only in its own copy)
        assertNotNull(original.body().selectFirst("header"),
                "Original should still have its header element after clone modification");
        // original should have no footer since clone's footer addition shouldn't affect it
        assertNull(original.body().selectFirst("footer"),
                "Original should not have footer element added to clone");
    }

    @Test
    @DisplayName("clone() preserves quirksMode and location fields, and clone.quirksMode changes do not affect original")
    public void test_TC04() {
        // GIVEN a new Document with non-default quirksMode
        Document original = new Document("http://host");
        original.quirksMode(Document.QuirksMode.quirks);
        assertEquals(Document.QuirksMode.quirks, original.quirksMode(),
                "Precondition failed: original quirksMode should be quirks");

        // WHEN clone() is called and then clone's quirksMode is changed
        Document copy = original.clone();
        // At this point copy should inherit original quirksMode and location
        // Now change clone's quirksMode to limitedQuirks
        copy.quirksMode(Document.QuirksMode.limitedQuirks);

        // THEN clone started with the same quirksMode and location as original
        assertEquals(Document.QuirksMode.quirks, copy.quirksMode(),
                "Clone should initially preserve the original quirksMode before mutation");
        assertEquals(original.location(), copy.location(),
                "Clone should preserve the original location field");
        // Changing clone's quirksMode does not affect the original
        assertEquals(Document.QuirksMode.quirks, original.quirksMode(),
                "Original quirksMode should remain unchanged after clone's modification");
        assertEquals(Document.QuirksMode.limitedQuirks, copy.quirksMode(),
                "Clone quirksMode should reflect its own modification");
    }
}