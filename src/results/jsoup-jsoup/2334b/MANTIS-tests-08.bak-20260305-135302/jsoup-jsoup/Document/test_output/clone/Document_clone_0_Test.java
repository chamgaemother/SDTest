package org.jsoup.nodes;

import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;
public class Document_clone_0_Test {

    @Test
    @DisplayName("TC01: clone() returns a new Document with identical structure but distinct outputSettings and parser instances")
    public void test_TC01() {
        // GIVEN: an original document shell with known baseUri and default UTF-8 charset/parser
        Document original = Document.createShell("http://example.com");
        // WHEN: cloning the document
        Document copy = original.clone(); // Changed to use the clone() method
        // THEN: the clone must be a distinct instance
        assertNotSame(original, copy, "Clone should not be the same instance as original");
        // AND: maintain the same baseUri and location
        assertEquals(original.baseUri(), copy.baseUri(), "Base URI should be preserved in clone");
        assertEquals(original.location(), copy.location(), "Location should be preserved in clone");
        // AND: outputSettings and parser must be deep-copied (distinct instances)
        assertNotSame(original.outputSettings(), copy.outputSettings(), "OutputSettings should be a different instance");
        assertNotSame(original.outputSettings().parser(), copy.outputSettings().parser(), "Parser should be a different instance"); // Updated to access parser correctly
    }

    @Test
    @DisplayName("TC02: Modifying original after clone does not affect the clone (deep isolation)")
    public void test_TC02() {
        // GIVEN: an original document with default charset UTF-8 and HTML parser
        Document original = Document.createShell("http://test");
        Document copy = original.clone(); // Changed to use the clone() method
        // Preconditions: copy has default charset UTF-8 and HTML parser
        assertEquals("UTF-8", copy.charset().name(), "Precondition: clone charset should start as UTF-8");
        assertEquals(Parser.HtmlTreeBuilder.class, copy.outputSettings().parser().getClass(), "Precondition: clone parser should start as HTML parser"); // Updated to access parser correctly
        // WHEN: mutate the original's charset and parser
        original.charset(Charset.forName("ISO-8859-1"));       // changes outputSettings on original only
        original.parser(Parser.xmlParser());                   // switches parser on original only
        // THEN: the clone's charset remains UTF-8 (deep isolation)
        assertEquals("UTF-8", copy.charset().name(), "Clone charset should remain unchanged at UTF-8");
        // AND: the clone's parser remains the original HTML parser, distinct from mutated original parser
        assertNotEquals(original.outputSettings().parser().getClass(), copy.outputSettings().parser().getClass(),
            "Clone parser type should remain HTML parser, not reflect XML parser of original"); // Updated to access parser correctly
    }
}