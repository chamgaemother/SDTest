package org.jsoup.parser;

import org.jsoup.parser.Parser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_parseInput_2_Test {

    @Test
    @DisplayName("Reader overload with real HtmlTreeBuilder parses HTML correctly (covers B_rd→B_exit)")
    public void test_TC08() {
        // Use htmlParser to exercise the Reader overload branch (B_rd)
        Parser parser = Parser.htmlParser();
        Reader input = new StringReader("<span>Test</span>");
        String baseUri = "http://example.com";

        // Invoke the Reader-based parseInput, expecting HTML parsing
        Document doc = parser.parseInput(input, baseUri);

        // Verify that a body with one <span> containing text "Test" is produced
        assertNotNull(doc, "Document should not be null");
        List<Element> spans = doc.body().select("span");
        assertEquals(1, spans.size(), "There should be exactly one <span> element");
        assertEquals("Test", spans.get(0).text(), "The <span> text content should match");
    }

    @Test
    @DisplayName("String overload with real XmlTreeBuilder via xmlParser parses XML fragment correctly (covers B_str→B_rd→B_exit)")
    public void test_TC09() {
        // Use xmlParser to exercise the String overload branch (B_str) which delegates to Reader path (B_rd)
        Parser parser = Parser.xmlParser();
        String xml = "<root><item>Value</item></root>";
        String baseUri = "";

        // Invoke the String-based parseInput, expecting XML parsing
        Document doc = parser.parseInput(xml, baseUri);

        // Verify structure: root element with a single <item> child containing "Value"
        assertNotNull(doc, "Document should not be null");
        Element root = doc.child(0);
        assertEquals("root", root.tagName(), "Root tag should be 'root'");
        Element item = root.child(0);
        assertEquals("item", item.tagName(), "Child tag should be 'item'");
        assertEquals("Value", item.text(), "The <item> text content should match");
    }
}