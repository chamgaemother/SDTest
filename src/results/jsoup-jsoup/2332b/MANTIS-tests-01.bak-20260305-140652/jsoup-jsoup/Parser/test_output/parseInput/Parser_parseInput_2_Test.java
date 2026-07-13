package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseInput_2_Test {

    @Test
    @DisplayName("parseInput(Reader,String) propagates RuntimeException from treeBuilder.parse")
    public void test_TC09() {
        // Arrange: stub TreeBuilder whose parse method always throws, to hit exception path B4→B6
        TreeBuilder stubBuilder = new HtmlTreeBuilder() {
            @Override
            public Document parse(Reader in, String baseUri, Parser parser) {
                throw new RuntimeException("boom");
            }
        };
        Parser p = new Parser(stubBuilder);
        Reader reader = new StringReader("<x/>"); // Fixed unclosed string literal
        String baseUri = "uri";

        // Act & Assert: the RuntimeException from stubBuilder.parse should propagate with the same message
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> p.parseInput(reader, baseUri),
                "Expected parseInput to propagate the RuntimeException");
        assertEquals("boom", ex.getMessage(), "Exception message should match stub");
    }

    @Test
    @DisplayName("parseInput(String,String) with XML parser and null baseUri returns Document with null baseUri")
    public void test_TC10() {
        // Arrange: use xmlParser and null baseUri to traverse string-overload path B1→B2→B4→B5
        String xml = "<root/>";
        String baseUri = null;
        Parser parser = Parser.xmlParser();

        // Act: parseInput on String overload
        Document doc = parser.parseInput(xml, baseUri);

        // Assert: document should not be null and its baseUri remains null as given
        assertNotNull(doc, "Document should be created for valid XML input");
        assertNull(doc.baseUri(), "baseUri should remain null when parser is called with null baseUri");
    }

    @Test
    @DisplayName("parseInput(String,String) returns null when underlying stub TreeBuilder.parse returns null")
    public void test_TC11() {
        // Arrange: stub TreeBuilder whose parse returns null, to cover boundary path B1→B2→B3
        TreeBuilder stubBuilder = new HtmlTreeBuilder() {
            @Override
            public Document parse(Reader in, String baseUri, Parser parser) {
                return null;
            }
        };
        Parser p = new Parser(stubBuilder);
        String html = "<empty/>";
        String baseUri = "u";

        // Act: parseInput on String overload
        Document result = p.parseInput(html, baseUri);

        // Assert: should return null when underlying parse returns null
        assertNull(result, "parseInput should return null when TreeBuilder.parse returns null");
    }
}