package org.jsoup.nodes;

import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for Document.clone() method based on scenarios TC01, TC02, and TC03.
 */
public class Document_clone_0_Test {

    @Test
    @DisplayName("TC01: clone() returns a distinct Document instance with identical basic structure")
    public void test_TC01() {
        // GIVEN a document shell with html, head, and body elements
        Document doc = Document.createShell("http://example.com");
        // WHEN cloning the document to exercise the deep copy path (B0->B1->B2)
        Document result = doc.clone();
        // THEN the clone should not be the same reference (distinct instance)
        assertNotSame(doc, result);
        // AND it should preserve the original location
        assertEquals("http://example.com", result.location());
        // AND the basic structure (head and body tags) remains intact in the clone
        assertEquals("head", result.head().tagName());
        assertEquals("body", result.body().tagName());
    }

    @Test
    @DisplayName("TC02: clone() produces a deep clone so modifications to clone’s outputSettings do not affect original")
    public void test_TC02() {
        // GIVEN a document shell to test outputSettings independence (default prettyPrint == true)
        Document doc = Document.createShell("/base");
        assertTrue(doc.outputSettings().prettyPrint(), "Precondition: original prettyPrint should be true");
        // WHEN cloning and modifying the clone's prettyPrint flag
        Document clone = doc.clone();  // exercises deep copy of OutputSettings
        clone.outputSettings().prettyPrint(false);
        // THEN the original document should retain prettyPrint = true
        assertTrue(doc.outputSettings().prettyPrint(), "Original should remain true after modifying clone");
        // AND the clone's prettyPrint should be false
        assertFalse(clone.outputSettings().prettyPrint(), "Clone should reflect the new false setting");
    }

    @Test
    @DisplayName("TC03: clone() produces a deep clone so modifying clone’s parser does not affect original parser")
    public void test_TC03() {
        // GIVEN a document shell to test parser independence
        Document doc = Document.createShell("u");
        // WHEN cloning the document to exercise deep clone of parser
        Document clone = doc.clone();
        // THEN the clone should have a different Parser instance than the original
        assertNotSame(doc.parser(), clone.parser(), "Parser instances should be distinct objects");
        // AND both parsers should be of the same implementation class (htmlParser)
        assertEquals(doc.parser().getClass(), clone.parser().getClass(), "Parser classes should match htmlParser class");
        // AND the original parser remains the default html parser
        assertEquals(Parser.htmlParser(), doc.parser(), "Original parser should still equal Parser.htmlParser()");
    }
}