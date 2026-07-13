package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.Reader;
import java.io.StringReader;
import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseInput_1_Test {

    @Test
    @DisplayName("parseInput(String, String) with XmlTreeBuilder and non-null inputs returns non-null Document")
    public void test_TC07() {
        // Scenario TC07: exercising parseInput(String, String) overload with XmlTreeBuilder (B0→B1→B2→B3→B4)
        // Choice of XmlTreeBuilder drives the XML parsing branch
        Parser p = new Parser(new XmlTreeBuilder());
        String xml = "<root><child/></root>";
        String baseUri = "http://example.com";
        // WHEN: parseInput should return a Document representing the XML structure
        Document doc = p.parseInput(xml, baseUri);
        // THEN: the returned Document is non-null and its first child is the <root> element
        assertNotNull(doc, "Expected non-null Document for valid XML input");
        Element firstChild = doc.child(0);
        assertEquals("root", firstChild.tagName(), "Expected root element tag name to be 'root'");
    }

    @Test
    @DisplayName("parseInput(String, String) with HtmlTreeBuilder and null baseUri throws NullPointerException")
    public void test_TC08() {
        // Scenario TC08: exercising parseInput(String, String) overload with HtmlTreeBuilder and null baseUri (B0→B1→B2→B5)
        // Null baseUri should trigger NullPointerException before or during parsing
        Parser p = new Parser(new HtmlTreeBuilder());
        String html = "<div/>";
        String baseUri = null;
        // WHEN/THEN: calling parseInput with null baseUri must throw NPE
        assertThrows(NullPointerException.class,
            () -> p.parseInput(html, baseUri),
            "Expected NullPointerException when baseUri is null");
    }

    @Test
    @DisplayName("parseInput(Reader, String) with HtmlTreeBuilder and null baseUri throws NullPointerException")
    public void test_TC09() {
        // Scenario TC09: exercising parseInput(Reader, String) overload with HtmlTreeBuilder and null baseUri (B0→B1→B4→B5)
        // Reader input path taken, with null baseUri leads to NullPointerException
        Parser p = new Parser(new HtmlTreeBuilder());
        Reader reader = new StringReader("<span/>"); // Fixed unclosed string literal
        String baseUri = null;
        // WHEN/THEN: calling parseInput with null baseUri must throw NPE
        assertThrows(NullPointerException.class,
            () -> p.parseInput(reader, baseUri),
            "Expected NullPointerException when baseUri is null for Reader overload");
    }
}