package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
public class Document_clone_2_Test {

    @Test
    @DisplayName("clone() deep-copies child element nodes so modifying clone’s child does not affect original")
    public void test_TC07() {
        // GIVEN: a shell document with an extra <div> child to test deep-copy of element nodes
        Document doc = Document.createShell("/test");
        Element extra = doc.appendElement("div");
        extra.attr("id", "orig");
        
        // WHEN: cloning the document and modifying the clone's child element
        Document clone = doc.clone();
        // clone.selectFirst("div") finds the deep-copied <div> in the clone
        clone.selectFirst("div").attr("id", "clone");
        
        // THEN: original document's <div> retains its original id, clone's reflects the new id
        assertEquals("orig", doc.selectFirst("div").attr("id"), 
            "Original document's child element should remain unchanged after clone modification");
        assertEquals("clone", clone.selectFirst("div").attr("id"), 
            "Cloned document's child element should reflect the updated attribute");
    }

    @Test
    @DisplayName("clone() deep-copies text nodes so setting text on clone does not clear original")
    public void test_TC08() {
        // GIVEN: a shell document with initial body text to test deep-copy of text nodes
        Document doc = Document.createShell("/base");
        doc.body().text("original");
        // Ensure precondition: body text is set to "original"
        assertEquals("original", doc.body().text(), 
            "Setup check: original document body text should be 'original'");
        
        // WHEN: cloning the document and setting text on the clone's body
        Document clone = doc.clone();
        // clone.body().text replaces only the clone's body text
        clone.body().text("cloneText");
        
        // THEN: original body text remains unchanged, clone reflects new text
        assertEquals("original", doc.body().text(), 
            "Original document's body text should remain 'original' after clone modification");
        assertEquals("cloneText", clone.body().text(), 
            "Cloned document's body text should be updated to 'cloneText'");
    }
}