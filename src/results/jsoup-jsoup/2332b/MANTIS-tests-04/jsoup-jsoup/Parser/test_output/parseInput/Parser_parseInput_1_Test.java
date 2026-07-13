package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.parser.ParseErrorList;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.TreeBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseInput_1_Test {

    @Test
    @DisplayName("TC06: parseInput(Reader, String) propagates exception from custom TreeBuilder.parse")
    public void test_TC06() {
        // Arrange: stub TreeBuilder that always throws on parse to hit the exception path B2→Exception
        TreeBuilder stub = new TreeBuilder() {
            @Override
            public Document parse(Reader in, String uri, Parser p) {
                throw new IllegalStateException("fail");
            }
            @Override
            public List<Node> parseFragment(Reader r, Element c, String u, Parser p) {
                return null;
            }
            @Override
            public ParseSettings defaultSettings() {
                // defaultSettings used in ctor path B0 only
                return ParseSettings.preserveCase;
            }
            @Override
            public TreeBuilder newInstance() {
                return this;
            }
            @Override
            public boolean isContentForTagData(String n) {
                return false;
            }
            @Override
            public String defaultNamespace() {
                return "";
            }
            @Override
            public void initialiseParse(Reader in, String uri, Parser p) {
            }
            @Override
            public void process(org.jsoup.parser.Token t) {
                // No operation, as this is a stub
            }
        };
        Parser parser = new Parser(new org.jsoup.parser.HtmlTreeBuilder()).setTreeBuilder(stub);
        Reader reader = new StringReader("x");

        // Act & Assert: expect the stub's IllegalStateException to propagate
        assertThrows(IllegalStateException.class, () -> parser.parseInput(reader, "http://base/"));
    }

    @Test
    @DisplayName("TC07: parseInput(String, String) via XML parser handles self-closing tags")
    public void test_TC07() {
        // Arrange: using xmlParser to parse a self-closing <div/> tag, expecting a single div element at root
        String xml = "<div/>";
        String baseUri = "http://xml/";
        Parser parser = Parser.xmlParser();

        // Act: parse the XML input
        Document doc = parser.parseInput(xml, baseUri);

        // Assert: the document's root element is <div>, covering the normal-success path B4
        Element root = doc.child(0); // xml parser produces root element directly
        assertEquals("div", root.nodeName(), "Expected root node name to be 'div'");
    }

    @Test
    @DisplayName("TC08: parseInput(String, String) with tracked errors collects up to maxErrors on malformed HTML")
    public void test_TC08() {
        // Arrange: malformed nesting "<div><span></div>" will generate parse errors, with maxErrors=2
        String html = "<div><span></div>";
        String baseUri = "http://example/";
        Parser parser = new Parser(new org.jsoup.parser.HtmlTreeBuilder()).setTrackErrors(2);
        // pre-check that tracking is enabled after setTrackErrors
        assertTrue(parser.isTrackErrors(), "Tracking should be enabled after setTrackErrors(2)");

        // Act: parse the malformed HTML
        Document doc = parser.parseInput(html, baseUri);

        // Assert: parser reports errors up to the configured max, and returns a non-null Document
        assertNotNull(doc, "Document should be returned even if malformed");
        ParseErrorList errors = parser.getErrors();
        assertTrue(errors.size() <= 2, "Error list size must not exceed maxErrors");
        assertTrue(parser.isTrackErrors(), "isTrackErrors() should remain true after parse");
    }

    @Test
    @DisplayName("TC09: parseInput(String, String) throws NullPointerException when baseUri is null")
    public void test_TC09() {
        // Arrange: passing null baseUri to trigger NPE in parseInput overload
        String html = "<p>text</p>";
        String baseUri = null;
        Parser parser = new Parser(new org.jsoup.parser.HtmlTreeBuilder());

        // Act & Assert: NullPointerException expected per contract when baseUri is null (B2→Exception)
        assertThrows(NullPointerException.class, () -> parser.parseInput(html, baseUri));
    }
}