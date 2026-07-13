package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.parser.TreeBuilder;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.ParseErrorList;
import org.jsoup.parser.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseInput_1_Test {

    @Test
    @DisplayName("TC07: parseInput(String, String) with real HtmlTreeBuilder produces a Document with expected element")
    public void test_TC07() {
        Parser parser = Parser.htmlParser();
        String html = "<div id=\"test\">Hello</div>";
        String baseUri = "http://example.com";

        Document doc = parser.parseInput(html, baseUri);
        Element div = doc.body().select("div#test").first();
        assertNotNull(div, "Expected a <div id=\"test\"> element in the parsed document body");
        assertEquals("div", div.tagName(), "Expected the parsed element to have tagName 'div'");
    }

    @Test
    @DisplayName("TC08: parseInput(Reader, String) with real XmlTreeBuilder parses XML root element")
    public void test_TC08() {
        Parser parser = Parser.xmlParser();
        Reader rdr = new StringReader("<root><child/></root>");
        String baseUri = "";

        Document doc = parser.parseInput(rdr, baseUri);
        Node firstChild = doc.child(0);
        assertEquals("root", firstChild.nodeName(),
                "Expected the XML parser to produce a root element named 'root'");
    }

    @Test
    @DisplayName("TC09: parseInput(String, String) propagates RuntimeException from TreeBuilder.parse")
    public void test_TC09() {
        // Stub TreeBuilder to throw in parse: exercise exception path
        TreeBuilder stub = new TreeBuilder() {
            @Override
            public Document parse(Reader in, String uri, Parser p) {
                process(null); // Call process method to satisfy the abstract requirement
                throw new IllegalStateException("parse failed");
            }

            @Override
            public Document parseFragment(Reader in, Element context, String baseUri, Parser p) {
                return null;
            }

            @Override
            public void process(Token token) { }

            @Override
            public ParseSettings defaultSettings() {
                return ParseSettings.preserveCase; // Return a valid ParseSettings object
            }

            @Override
            public TreeBuilder newInstance() {
                return this;
            }

            @Override
            public boolean isContentForTagData(String normalName) {
                return false;
            }

            @Override
            public String defaultNamespace() {
                return "";
            }
        };
        Parser parser = new Parser(stub);
        String html = "<x/>";
        String baseUri = "u";

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> parser.parseInput(html, baseUri),
                "Expected parseInput to propagate IllegalStateException from the TreeBuilder");
        assertEquals("parse failed", ex.getMessage(),
                "Expected exception message to be 'parse failed'");
    }

    @Test
    @DisplayName("TC10: parseInput(String, String) with null baseUri throws NullPointerException")
    public void test_TC10() {
        Parser parser = Parser.htmlParser();
        String html = "<p>Test</p>";
        String baseUri = null;

        assertThrows(NullPointerException.class,
                () -> parser.parseInput(html, baseUri),
                "Expected parseInput to throw NullPointerException when baseUri is null");
    }
}