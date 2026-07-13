package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_parseInput_0_Test {

    @Test
    @DisplayName("TC01_O1: parseInput(String,String) with empty HTML returns a minimal Document with html, head, and empty body")
    public void test_TC01_O1() {
        // branch: html non-null, baseUri non-null -> use overload(String,String)
        Parser parser = Parser.htmlParser();
        String html = "";
        String baseUri = "http://example.com/";
        Document doc = parser.parseInput(html, baseUri);
        // Expect minimal html structure even for empty input
        assertEquals("<html><head></head><body></body></html>", doc.html());
        assertEquals(baseUri, doc.baseUri());
    }

    @Test
    @DisplayName("TC02_O1: parseInput(String,String) with simple HTML containing a single element returns that element in body")
    public void test_TC02_O1() {
        // branch: html non-null, baseUri non-null -> simple HTML parse
        Parser parser = Parser.htmlParser();
        String html = "<p>Hello</p>";
        String baseUri = "http://host/";
        Document doc = parser.parseInput(html, baseUri);
        Element p = doc.body().selectFirst("p");
        assertNotNull(p, "Expected a <p> element in the body");
        assertEquals("Hello", p.text());
        assertEquals(baseUri, doc.baseUri());
    }

    @Test
    @DisplayName("TC03_O1: parseInput(String,String) with null HTML throws NullPointerException")
    public void test_TC03_O1() {
        // branch: html null -> expect NPE before parsing
        Parser parser = Parser.htmlParser();
        String html = null;
        String baseUri = "http://x/";
        assertThrows(NullPointerException.class, () -> parser.parseInput(html, baseUri));
    }

    @Test
    @DisplayName("TC04_O2: parseInput(Reader,String) with simple XML input using XmlTreeBuilder returns XML elements")
    public void test_TC04_O2() {
        // branch: overload Reader,String and xml builder -> xmlParser()
        Parser parser = Parser.xmlParser();
        Reader reader = new StringReader("<root><child/></root>");
        String baseUri = "";
        Document doc = parser.parseInput(reader, baseUri);
        // Expect first child node name "root"
        Node rootNode = doc.childNode(0);
        assertEquals("root", rootNode.nodeName());
        // Expect nested child element tagName() == "child"
        Element child = doc.selectFirst("child");
        assertNotNull(child, "Expected <child> element in parsed XML");
        assertEquals("child", child.tagName());
    }

    @Test
    @DisplayName("TC05_O2: parseInput(Reader,String) with null Reader throws NullPointerException")
    public void test_TC05_O2() {
        // branch: reader null -> expect NPE
        Parser parser = Parser.htmlParser();
        Reader reader = null;
        String baseUri = "u";
        assertThrows(NullPointerException.class, () -> parser.parseInput(reader, baseUri));
    }
}