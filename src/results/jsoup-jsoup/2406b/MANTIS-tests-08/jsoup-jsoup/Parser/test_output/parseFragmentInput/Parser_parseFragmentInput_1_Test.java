package org.jsoup.parser;

import org.jsoup.parser.Parser;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class Parser_parseFragmentInput_1_Test {

    @Test
    @DisplayName("TC11: String overload with real htmlParser and non-null context returns actual parsed elements")
    public void test_TC11() {
        // Use htmlParser and non-null context to take the <p> fragment path B0→B1→B3→B5→B7→B8
        Parser parser = Parser.htmlParser();
        String fragment = "<p>one</p>";
        Element ctx = new Element("div"); // non-null context triggers HTML fragment parsing
        String baseUri = "http://example.com";

        List<Node> result = parser.parseFragmentInput(fragment, ctx, baseUri);

        // Expect one <p> element with text "one"
        assertEquals(1, result.size(), "Should parse a single element");
        Element p = (Element) result.get(0);
        assertEquals("p", p.tagName(), "Tag name should be 'p'");
        assertEquals("one", p.text(), "Text content should be 'one'");
    }

    @Test
    @DisplayName("TC12: Reader overload with real htmlParser and null context returns parsed nodes at document fragment root")
    public void test_TC12() {
        // Use htmlParser with null context to get top-level fragment nodes B0→B1→B3→B5→B7→B8
        Parser parser = Parser.htmlParser();
        Reader reader = new StringReader("<a></a><b></b>");
        Element ctx = null; // null context to parse as standalone fragment
        String baseUri = "http://x";

        List<Node> result = parser.parseFragmentInput(reader, ctx, baseUri);

        // Expect two elements <a> and <b> in order
        assertEquals(2, result.size(), "Should parse two elements");
        Element a = (Element) result.get(0);
        Element b = (Element) result.get(1);
        assertEquals("a", a.tagName(), "First tag should be 'a'");
        assertEquals("b", b.tagName(), "Second tag should be 'b'");
    }

    @Test
    @DisplayName("TC13: Reader overload with real xmlParser and non-null context (ignored) returns XML nodes")
    public void test_TC13() {
        // Use xmlParser; context should be ignored for XML parsing path B0→B1→B3→B5→B7→B8
        Parser parser = Parser.xmlParser();
        String xml = "<item id=\"1\">X</item>";
        Reader reader = new StringReader(xml);
        Element ctx = new Element("ignore"); // context ignored by XML parser
        String baseUri = "";

        List<Node> result = parser.parseFragmentInput(reader, ctx, baseUri);

        // Expect one <item> element with attribute id="1"
        assertEquals(1, result.size(), "Should parse a single XML element");
        Element item = (Element) result.get(0);
        assertEquals("item", item.tagName(), "Tag name should be 'item'");
        assertEquals("1", item.attr("id"), "Attribute id should be '1'");
    }
}