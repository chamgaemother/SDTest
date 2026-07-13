package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class Parser_parseInput_2_Test {

    @Test
    @DisplayName("xmlParser.parseInput(String, String) with simple XML string invokes the String overload then Reader overload and returns XML document")
    public void test_TC08() {
        // Given: an XML parser and a simple XML string input; this should take the String overload path B2 then B3
        Parser parser = Parser.xmlParser();
        String xml = "<root><child attr='v'/></root>";
        String baseUri = "";
        
        // When: parsing via the String overload
        Document doc = parser.parseInput(xml, baseUri);
        
        // Then: the document should have a root element with a child having attribute 'attr'='v'
        // This verifies that parseInput(String, String) forwarded to parseInput(Reader, String) and built an XML tree
        Element root = doc.child(0);
        assertEquals("root", root.nodeName(), "Expected root element name to be 'root'");
        Element child = doc.selectFirst("child");
        assertEquals("v", child.attr("attr"), "Expected child element to have attribute 'attr' with value 'v'");
    }

    @Test
    @DisplayName("htmlParser.parseInput(Reader, String) with simple HTML reader invokes Reader overload directly and returns HTML document")
    public void test_TC09() {
        // Given: an HTML parser and a Reader input; this should take the Reader overload path B3 directly
        Parser parser = Parser.htmlParser();
        Reader reader = new StringReader("<div>Test</div>");
        String baseUri = "http://example.com/";
        
        // When: parsing via the Reader overload
        Document doc = parser.parseInput(reader, baseUri);
        
        // Then: the document body should contain the div with text 'Test', and baseUri should be preserved
        assertEquals("Test", doc.body().selectFirst("div").text(), "Expected body div text to be 'Test'");
        assertEquals(baseUri, doc.baseUri(), "Expected document baseUri to match the provided baseUri");
    }
}