package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Document_clone_0_Test {

    @Test
    @DisplayName("clone() on empty Document produces a distinct deep copy with no child nodes (loop-0 children)")
    public void test_TC01() {
        // GIVEN: an empty document with no children triggers the branch where childNodes list is empty
        Document doc = new Document("http://example.com");
        assertTrue(doc.childNodes().isEmpty(), "Precondition: original document has no child nodes");
        
        // WHEN: perform clone
        Document clone = doc.clone();
        
        // THEN: verify deep copy properties
        assertNotSame(doc, clone, "clone should be a different instance");
        assertTrue(clone.childNodes().isEmpty(), "clone should have zero child nodes matching original empty state");
        assertEquals(doc.baseUri(), clone.baseUri(), "baseUri should be preserved in clone");
        // outputSettings and parser should be cloned, not same instances
        assertNotSame(doc.outputSettings(), clone.outputSettings(), "outputSettings should be a distinct instance");
        assertNotSame(doc.parser(), clone.parser(), "parser should be a distinct instance");
    }

    @Test
    @DisplayName("clone() on Document with multiple children produces deep copies of all children (loop-N children)")
    public void test_TC02() {
        // GIVEN: a document shell with multiple children under root (html element, then its head and body)
        Document doc = Document.createShell("http://test");
        List<org.jsoup.nodes.Node> originalChildren = doc.childNodes();
        assertFalse(originalChildren.isEmpty(), "Precondition: document shell should have child nodes");
        int size = originalChildren.size();
        
        // WHEN: perform clone
        Document clone = doc.clone();
        List<org.jsoup.nodes.Node> cloneChildren = clone.childNodes();
        
        // THEN: verify same number of children and deep copy of each
        assertEquals(size, cloneChildren.size(), "clone should have same number of child nodes as original");
        for (int i = 0; i < size; i++) {
            org.jsoup.nodes.Node origNode = originalChildren.get(i);
            org.jsoup.nodes.Node clonedNode = cloneChildren.get(i);
            assertNotSame(origNode, clonedNode, "each child node should be a different instance");
            assertEquals(origNode.outerHtml(), clonedNode.outerHtml(), 
                         "each cloned node's HTML should equal the original's HTML");
        }
    }

    @Test
    @DisplayName("clone() yields a separate outputSettings instance (modify clone.outputSettings, original unaffected)")
    public void test_TC03() {
        // GIVEN: a document with outputSettings.prettyPrint set to false
        Document doc = new Document("u");
        doc.outputSettings().prettyPrint(false);
        assertFalse(doc.outputSettings().prettyPrint(), "Precondition: original prettyPrint should be false");
        
        // WHEN: clone and modify clone's outputSettings
        Document clone = doc.clone();
        assertFalse(clone.outputSettings().prettyPrint(), "clone initial prettyPrint should match original (false)");
        clone.outputSettings().prettyPrint(true);
        
        // THEN: original's outputSettings should remain unaffected
        assertFalse(doc.outputSettings().prettyPrint(), 
            "modifying clone's prettyPrint should not affect original's prettyPrint");
    }

    @Test
    @DisplayName("clone() yields a separate parser instance (modify clone.parser, original unaffected)")
    public void test_TC04() {
        // GIVEN: a document with parser set to XML parser
        Document doc = new Document("base");
        doc.parser(org.jsoup.parser.Parser.xmlParser());
        String originalParserName = doc.parser().getName();
        assertEquals(org.jsoup.parser.Parser.xmlParser().getName(), originalParserName, 
            "Precondition: document parser should be XML parser");
        
        // WHEN: clone and modify clone's parser
        Document clone = doc.clone();
        assertEquals(originalParserName, clone.parser().getName(), 
            "clone initial parser name should match original parser name");
        clone.parser(org.jsoup.parser.Parser.htmlParser());
        
        // THEN: original's parser should remain unchanged
        assertEquals(originalParserName, doc.parser().getName(), 
            "modifying clone's parser should not affect original's parser");
    }
}