package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.parser.TreeBuilder;
import org.jsoup.parser.HtmlTreeBuilder;
import org.jsoup.parser.XmlTreeBuilder;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_parseInput_0_Test {

    /**
     * A stub TreeBuilder whose parse method always throws IllegalStateException("fail")
     */
    private static class StubTreeBuilder extends HtmlTreeBuilder {
        @Override
        public Document parse(Reader input, String baseUri, Parser parser) {
            throw new IllegalStateException("fail");
        }
    }

    @Test
    @DisplayName("parseInput(String,baseUri) with empty HTML returns an empty Document with provided baseUri")
    public void test_TC01_O1() {
        // Given: empty HTML string and a baseUri
        Parser parser = new Parser(new HtmlTreeBuilder()); // branch B1
        String html = "";
        String baseUri = "http://example.com/";
        // When: parsing empty HTML
        Document doc = parser.parseInput(html, baseUri); // branch B2: normal flow
        // Then: baseUri is retained and no body children
        assertAll(
            () -> assertEquals(baseUri, doc.baseUri(), "Base URI should match the provided value"),
            () -> assertTrue(doc.body().children().isEmpty(), "Body should have no children for empty input")
        );
    }

    @Test
    @DisplayName("parseInput(Reader,baseUri) with simple HTML returns Document with one child element")
    public void test_TC02_O2() {
        // Given: simple HTML Reader and baseUri
        Parser parser = new Parser(new HtmlTreeBuilder()); // branch B1
        Reader input = new StringReader("<p>Hello</p>");
        String baseUri = "base";
        // When: parsing via Reader overload
        Document doc = parser.parseInput(input, baseUri); // branch B2: normal flow
        // Then: body contains one <p> element
        Element p = doc.body().child(0);
        assertEquals("p", p.tagName(), "Expected a <p> element as the first child");
    }

    @Test
    @DisplayName("parseInput(String,null) with null baseUri throws IllegalArgumentException for undefined baseUri")
    public void test_TC03_O1() {
        // Given: HTML string and null baseUri => should validate baseUri non-null
        Parser parser = new Parser(new HtmlTreeBuilder()); // branch B1
        String html = "<div/>";
        String baseUri = null;
        // When/Then: expecting IllegalArgumentException for null baseUri
        assertThrows(IllegalArgumentException.class, () -> parser.parseInput(html, baseUri),
            "parseInput should throw IllegalArgumentException when baseUri is null");
    }

    @Test
    @DisplayName("parseInput(Reader,baseUri) with null Reader throws NullPointerException")
    public void test_TC04_O2() {
        // Given: null Reader => should immediately NPE when calling parse
        Parser parser = new Parser(new HtmlTreeBuilder()); // branch B1
        Reader input = null;
        String baseUri = "u";
        // When/Then: expecting NullPointerException for null Reader
        assertThrows(NullPointerException.class, () -> parser.parseInput(input, baseUri),
            "parseInput should throw NullPointerException when Reader is null");
    }

    @Test
    @DisplayName("parseInput(String,baseUri) with stubbed TreeBuilder throwing IllegalStateException propagates exception")
    public void test_TC05_O1() {
        // Given: parser with stubbed TreeBuilder that fails in parse
        Parser parser = new Parser(new StubTreeBuilder()); // branch B1 + mock-stub path
        String html = "<x/>";
        String baseUri = "b";
        // When/Then: IllegalStateException("fail") propagates
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> parser.parseInput(html, baseUri),
            "Expected IllegalStateException from stubbed TreeBuilder");
        assertEquals("fail", ex.getMessage(), "Exception message should propagate from stub");
    }

    @Test
    @DisplayName("parseInput(String,baseUri) after setTreeBuilder(XmlTreeBuilder) returns XML Document root element")
    public void test_TC06_O1() {
        // Given: parser switched to XML mode => will parse self-closing tags as XML
        Parser parser = new Parser(new HtmlTreeBuilder()); // initial builder
        parser.setTreeBuilder(new XmlTreeBuilder()); // branch B2 variant-xml
        String xml = "<root/>";
        String baseUri = "uri";
        // When: parsing XML input
        Document doc = parser.parseInput(xml, baseUri);
        // Then: first child nodeName is "root"
        Node root = doc.child(0);
        assertEquals("root", root.nodeName(), "XML parser should produce a <root/> element as first child");
    }

    @Test
    @DisplayName("parseInput(Reader,baseUri) with page containing nested tags returns correct nested hierarchy")
    public void test_TC07_O2() {
        // Given: nested <div><span> structure -> ensures nested parsing branch
        Parser parser = new Parser(new HtmlTreeBuilder()); // branch B1
        Reader r = new StringReader("<div><span>x</span></div>");
        String uri = "u";
        // When: parse via Reader
        Document doc = parser.parseInput(r, uri); // branch B2
        // Then: one match for "div > span"
        Elements matches = doc.select("div > span"); // Changed from List<Node> to Elements
        assertEquals(1, matches.size(), "Should find exactly one nested span under div");
    }

    @Test
    @DisplayName("parseInput(String,baseUri) with extremely large HTML string returns Document without OutOfMemoryError")
    public void test_TC08_O1() {
        // Given: very large HTML to test performance boundary, not throwing OOME
        Parser parser = new Parser(new HtmlTreeBuilder()); // branch B1
        StringBuilder sb = new StringBuilder("<p>");
        for (int i = 0; i < 1_000_000; i++) {
            sb.append('a');
        }
        sb.append("</p>");
        String html = sb.toString();
        String uri = "u";
        // When: parse large input
        Document doc = parser.parseInput(html, uri); // branch B2
        // Then: body is accessible and not null
        assertNotNull(doc.body(), "Body should be accessible even for large input");
    }

    @Test
    @DisplayName("parseInput(Reader,baseUri) with empty baseUri string returns Document with empty baseUri")
    public void test_TC09_O2() {
        // Given: empty baseUri string => boundary case
        Parser parser = new Parser(new HtmlTreeBuilder()); // branch B1
        Reader r = new StringReader("<p/>");
        String uri = "";
        // When: parse via Reader
        Document doc = parser.parseInput(r, uri); // branch B2
        // Then: baseUri is exactly empty
        assertEquals("", doc.baseUri(), "Base URI should be empty string when supplied as such");
    }

    @Test
    @DisplayName("parseInput(String,baseUri) with HTML containing attributes returns correct attribute values")
    public void test_TC10_O1() {
        // Given: HTML with <img src='a.png'/> to test attribute parsing
        Parser parser = new Parser(new HtmlTreeBuilder()); // branch B1
        String html = "<img src='a.png'/>";
        String uri = "u";
        // When: parse and select img element
        Document doc = parser.parseInput(html, uri); // branch B2
        String src = doc.select("img").attr("src");
        // Then: src attribute is correctly parsed
        assertEquals("a.png", src, "Attribute 'src' should be parsed as 'a.png'");
    }
}