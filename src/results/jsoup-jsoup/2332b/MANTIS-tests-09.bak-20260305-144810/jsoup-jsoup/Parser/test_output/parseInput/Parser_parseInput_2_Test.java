package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseInput_2_Test {

    /**
     * A stub TreeBuilder that always throws IllegalStateException when parse is invoked.
     */
    static class DummyTreeBuilderExState extends TreeBuilder {
        @Override
        public Document parse(Reader input, String baseUri, Parser parser) {
            // Simulate an unexpected runtime error deep in parsing
            throw new IllegalStateException("stub state exception");
        }

        @Override
        public List<Node> parseFragment(Reader reader, Element context, String baseUri, Parser parser) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void process(org.jsoup.parser.Token token) {
            // Dummy implementation for process method
        }
    }

    /**
     * A stub TreeBuilder that always throws IllegalArgumentException when parse is invoked.
     */
    static class DummyTreeBuilderExArg extends TreeBuilder {
        @Override
        public Document parse(Reader input, String baseUri, Parser parser) {
            // Simulate an invalid argument scenario deep in parsing
            throw new IllegalArgumentException("stub argument exception");
        }

        @Override
        public List<Node> parseFragment(Reader reader, Element context, String baseUri, Parser parser) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void process(org.jsoup.parser.Token token) {
            // Dummy implementation for process method
        }
    }

    @Test
    @DisplayName("TC10: parseInput(Reader,String) propagates unchecked exception from custom TreeBuilder.parse")
    public void test_TC10() {
        // GIVEN a parser whose TreeBuilder.parse throws IllegalStateException
        TreeBuilder stub = new DummyTreeBuilderExState();
        Parser parser = new Parser(stub);
        Reader reader = new StringReader("<html></html>");
        String baseUri = "base";
        // WHEN / THEN: invoking parseInput should propagate the stub's unchecked exception
        // This tests the branch where control goes into parse(Reader,String,Parser) and immediately throws
        assertThrows(IllegalStateException.class, () -> parser.parseInput(reader, baseUri));
    }

    @Test
    @DisplayName("TC11: xmlParser.parseInput(Reader,String) delegates to XmlTreeBuilder.parse and returns Document")
    public void test_TC11() {
        // GIVEN a well-formed XML fragment to preserve case-sensitive structure
        Reader reader = new StringReader("<Item TYPE=\"X\">val</Item>");
        String baseUri = "";
        // WHEN: parseInput via xmlParser
        Document doc = Parser.xmlParser().parseInput(reader, baseUri);
        // THEN: the returned Document must contain the exact tag name "Item" and preserve attribute case
        // This covers the branch for XML parsing path (B0→B3→B5)
        Element item = doc.selectFirst("Item");
        assertNotNull(item, "Expected an <Item> element in the parsed XML");
        assertEquals("X", item.attr("TYPE"), "Attribute TYPE should be preserved in XML parsing");
        assertEquals("val", item.text(), "Text content inside <Item> should be 'val'");
    }

    @Test
    @DisplayName("TC12: parseInput(String,String) propagates unchecked exception from custom TreeBuilder.parse on string overload")
    public void test_TC12() {
        // GIVEN a parser whose TreeBuilder.parse throws IllegalArgumentException
        TreeBuilder stub = new DummyTreeBuilderExArg();
        Parser parser = new Parser(stub);
        String html = "<p></p>";
        String baseUri = "base";
        // WHEN / THEN: invoking the string overload should propagate the stub's unchecked exception
        // This tests the branch B0→B1 (string overload) then into parse(Reader,String,Parser) throwing
        assertThrows(IllegalArgumentException.class, () -> parser.parseInput(html, baseUri));
    }
}