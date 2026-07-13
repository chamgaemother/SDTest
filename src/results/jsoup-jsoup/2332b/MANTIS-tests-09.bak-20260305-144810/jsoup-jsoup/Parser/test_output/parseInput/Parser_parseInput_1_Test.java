package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_parseInput_1_Test {

    @Test
    @DisplayName("parseInput(String, String) with HtmlTreeBuilder parses simple HTML string into Document element tree")
    public void test_TC08() {
        // Using htmlParser ensures path goes through B0 (public parseInput), B1 (string overload), B3 (Reader overload), B5 (HtmlTreeBuilder.parse)
        String html = "<div>jsoup</div>";
        String baseUri = "http://example.org";

        // WHEN: parse simple HTML
        Document doc = Parser.htmlParser().parseInput(html, baseUri);

        // THEN: document should contain the div with text jsoup and correct baseUri
        Element div = doc.selectFirst("div");
        assertNotNull(div, "Expected a <div> element in the parsed document");
        assertEquals("jsoup", div.text(), "The text of the div element should be 'jsoup'");
        assertEquals(baseUri, doc.baseUri(), "The baseUri should be preserved on the Document");
    }

    @Test
    @DisplayName("parseInput(String, String) with XmlTreeBuilder via xmlParser preserves element case and structure")
    public void test_TC09() {
        // Using xmlParser ensures path goes through B0 (public parseInput), B1 (string overload), B3 (Reader overload), B5 (XmlTreeBuilder.parse)
        String xml = "<Item TYPE=\"example\">123</Item>";
        String baseUri = "";

        // WHEN: parse XML with case-sensitive builder
        Document doc = Parser.xmlParser().parseInput(xml, baseUri);

        // THEN: document should contain the <Item> element exactly as provided
        Element item = doc.selectFirst("Item");
        assertNotNull(item, "Expected an <Item> element in the parsed document");
        // Attribute names and values should be preserved
        assertEquals("example", item.attr("TYPE"), "The TYPE attribute should be 'example'");
        // Text content should be correctly parsed
        assertEquals("123", item.text(), "The text content of <Item> should be '123'");
    }
}