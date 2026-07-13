package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.HtmlTreeBuilder;
import org.jsoup.parser.Parser;
import org.jsoup.parser.XmlTreeBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseInput_2_Test {

    @Test
    @DisplayName("TC10: parseInput(String, String) with real HtmlTreeBuilder returns a Document with expected element structure")
    public void test_TC10() {
        Parser p = new Parser(new HtmlTreeBuilder());
        String html = "<div>hello</div>";
        String baseUri = "http://example.com";
        Document doc = p.parseInput(html, baseUri);
        assertNotNull(doc, "Expected non-null Document");
        Element div = doc.selectFirst("div");
        assertNotNull(div, "Expected a <div> element in the document");
        assertEquals("hello", div.text(), "Expected the <div> to contain text 'hello'");
    }

    @Test
    @DisplayName("TC11: parseInput(Reader, String) with real HtmlTreeBuilder returns a Document with expected element hierarchy")
    public void test_TC11() {
        Parser p = new Parser(new HtmlTreeBuilder());
        // Fixed the unclosed string literal by adding the missing closing quote
        Reader reader = new StringReader("<span id='x'/>");
        String baseUri = "";
        Document doc = p.parseInput(reader, baseUri);
        assertNotNull(doc, "Expected non-null Document");
        Element span = doc.selectFirst("span");
        assertNotNull(span, "Expected a <span> element in the document");
        assertEquals("x", span.attr("id"), "Expected the <span> to have id='x'");
    }

    @Test
    @DisplayName("TC12: parseInput(Reader, String) with real XmlTreeBuilder returns a Document with correct XML node names")
    public void test_TC12() {
        Parser p = new Parser(new XmlTreeBuilder());
        Reader reader = new StringReader("<root><leaf/></root>");
        String baseUri = "urn:test";
        Document doc = p.parseInput(reader, baseUri);
        assertNotNull(doc, "Expected non-null Document");
        Element root = doc.child(0);
        assertNotNull(root, "Expected a root element");
        assertEquals("root", root.tagName(), "Expected the root element tagName to be 'root'");
    }

    @Test
    @DisplayName("TC13: parseInput(String, String) with empty html and non-null baseUri returns empty Document")
    public void test_TC13() {
        Parser p = new Parser(new HtmlTreeBuilder());
        String html = "";
        String baseUri = "base";
        Document doc = p.parseInput(html, baseUri);
        assertNotNull(doc, "Expected non-null Document");
        assertEquals(0, doc.childNodeSize(), "Expected the document to have no child nodes for empty input");
    }
}