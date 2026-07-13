package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseInput_0_Test {

    @Test
    @DisplayName("TC01_O1: parseInput(String html, String baseUri) returns a Document with parsed <p> element when given simple HTML")
    public void test_TC01_O1() {
        // GIVEN: a default HTML parser and simple <p> text
        Parser parser = Parser.htmlParser();
        String html = "<p>text</p>";
        String base = "http://example.com";
        // WHEN: parsing via the String overload → exercises B0→B1→B2 path
        Document doc = parser.parseInput(html, base);
        // THEN: the document should contain a <p> with the correct text and baseUri
        assertAll(
            () -> assertEquals("text", doc.select("p").text(), "Expected paragraph text to match"),
            () -> assertEquals(base, doc.baseUri(), "Expected baseUri to be preserved")
        );
    }

    @Test
    @DisplayName("TC02_O2: parseInput(Reader inputHtml, String baseUri) returns a Document with parsed <div> element for Reader input")
    public void test_TC02_O2() {
        // GIVEN: a default HTML parser and Reader providing <div>hello</div>
        Parser parser = Parser.htmlParser();
        Reader reader = new StringReader("<div>hello</div>");
        String base = "";
        // WHEN: parsing via the Reader overload → exercises B0→B3→B2 path
        Document doc = parser.parseInput(reader, base);
        // THEN: the document should contain a <div> with the text "hello"
        assertEquals("hello", doc.select("div").text(), "Expected div text to match");
    }

    @Test
    @DisplayName("TC03_O1: parseInput(String html, String baseUri) throws NullPointerException when html is null")
    public void test_TC03_O1() {
        // GIVEN: a default HTML parser and a null html string
        Parser parser = Parser.htmlParser();
        String html = null;
        String base = "http://x";
        // WHEN/THEN: parsing null html must throw NPE → exercises B0→B1→B4 path
        assertThrows(NullPointerException.class, () -> parser.parseInput(html, base),
            "Expected parseInput(String,...) to throw NullPointerException when html is null");
    }

    @Test
    @DisplayName("TC04_O2: parseInput(Reader inputHtml, String baseUri) throws IllegalArgumentException when inputHtml is null")
    public void test_TC04_O2() {
        // GIVEN: a default HTML parser and a null Reader
        Parser parser = Parser.htmlParser();
        Reader reader = null;
        String base = "";
        // WHEN/THEN: parsing null Reader must throw IllegalArgumentException by contract → exercises B0→B3→B4 path
        assertThrows(IllegalArgumentException.class, () -> parser.parseInput(reader, base),
            "Expected parseInput(Reader,...) to throw IllegalArgumentException when Reader is null");
    }

    @Test
    @DisplayName("TC05_O2: parseInput(Reader inputHtml, String baseUri) propagates treeBuilder.parse exception from stubbed TreeBuilder")
    public void test_TC05_O2() {
        // GIVEN: a parser with a stubbed TreeBuilder that always throws IllegalStateException
        Parser parser = Parser.htmlParser();
        TreeBuilder stubBuilder = new TreeBuilder() {
            @Override
            public Document parse(Reader input, String baseUri, Parser parser) throws IllegalStateException {
                throw new IllegalStateException("stub parse fail");
            }
            @Override
            public Document parse(String html, String baseUri, Parser parser) throws UnsupportedOperationException {
                throw new UnsupportedOperationException();
            }
            @Override
            public List<Node> parseFragment(Reader input, Element context, String baseUri, Parser parser) throws UnsupportedOperationException {
                throw new UnsupportedOperationException();
            }
            @Override
            public ParseSettings defaultSettings() {
                return parser.settings();
            }
            @Override
            public TreeBuilder newInstance() {
                return this;
            }
            @Override
            public void process(Token token) { /* implementation */ }
        };
        parser.setTreeBuilder(stubBuilder);
        Reader reader = new StringReader("irrelevant");
        // WHEN/THEN: the stub's parse exception must propagate → exercises B0→B3→B5 path
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> parser.parseInput(reader, "http://base"),
            "Expected stub parse failure to propagate");
        assertEquals("stub parse fail", ex.getMessage(), "Expected exception message from stub");
    }
}