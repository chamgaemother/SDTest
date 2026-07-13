package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.parser.TreeBuilder;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.ParseSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseInput_1_Test {

    @Test
    @DisplayName("parseInput(String, String) throws NullPointerException when html input is null")
    public void test_TC05() {
        // GIVEN a parser and null HTML input to force the null-check path in parseInput(String, String)
        Parser parser = Parser.htmlParser();
        String html = null;
        String baseUri = "http://example.com";
        // WHEN & THEN expect a NullPointerException because html is null
        assertThrows(NullPointerException.class, () -> parser.parseInput(html, baseUri));
    }

    @Test
    @DisplayName("parseInput(Reader, String) returns a Document when using xmlParser and simple XML content")
    public void test_TC06() {
        // GIVEN a simple XML content reader to follow the xml parsing branch
        Reader input = new StringReader("<root><child/></root>");
        String baseUri = "file://local";
        Parser parser = Parser.xmlParser();
        // WHEN parsing the XML content
        Document doc = parser.parseInput(input, baseUri);
        // THEN document must not be null and root element name must be 'root'
        assertNotNull(doc, "Expected non-null Document from xmlParser");
        assertEquals("root", doc.child(0).nodeName(), "Root element name should be 'root'");
    }

    @Test
    @DisplayName("parseInput(Reader, String) propagates a RuntimeException from a custom TreeBuilder.parse")
    public void test_TC07() {
        // GIVEN a stub TreeBuilder that always throws IllegalStateException to force exception propagation
        TreeBuilder stubBuilder = new TreeBuilder() {
            @Override public ParseSettings defaultSettings() { return ParseSettings.defaultSettings(); }
            @Override public TreeBuilder newInstance() { return this; }
            @Override public Document parse(Reader r, String baseUri, Parser parser) {
                throw new IllegalStateException("fail-runtime");
            }
            @Override public List<Node> parseFragment(Reader r, Element context, String baseUri, Parser parser) {
                return null;
            }
            @Override public void process(Token token) { /* Implementation required for abstract method */ }
        };
        Parser parser = new Parser(stubBuilder);
        Reader input = new StringReader("<p>test</p>");
        String baseUri = "http://x";
        // WHEN & THEN calling parseInput should throw the same IllegalStateException with the original message
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> parser.parseInput(input, baseUri));
        assertEquals("fail-runtime", ex.getMessage(), "Exception message should be propagated unchanged");
    }
}