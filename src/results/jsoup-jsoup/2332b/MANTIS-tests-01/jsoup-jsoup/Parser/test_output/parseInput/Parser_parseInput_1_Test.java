package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
public class Parser_parseInput_1_Test {

    @Test
    @DisplayName("parseInput(Reader, String) throws NullPointerException when reader is null")
    public void test_TC06_O2_nullReader() {
        // GIVEN a real HTML parser and a null Reader to trigger reader null check (path B0->B2->exception)
        Parser parser = Parser.htmlParser();
        Reader reader = null;
        String baseUri = "http://example.com";
        // WHEN/THEN expecting NullPointerException due to null reader
        assertThrows(NullPointerException.class, () -> parser.parseInput(reader, baseUri));
    }

    @Test
    @DisplayName("parseInput(Reader, String) throws NullPointerException when baseUri is null")
    public void test_TC07_O2_nullBaseUri() {
        // GIVEN a real HTML parser and a valid Reader but null baseUri to trigger baseUri null check (path B0->B2->exception)
        Parser parser = Parser.htmlParser();
        Reader reader = new StringReader("<p/>");
        String baseUri = null;
        // WHEN/THEN expecting NullPointerException due to null baseUri
        assertThrows(NullPointerException.class, () -> parser.parseInput(reader, baseUri));
    }

    @Test
    @DisplayName("parseInput(Reader, String) with XmlTreeBuilder returns Document containing XML element")
    public void test_TC08_O2_xmlParser_success() {
        // GIVEN an XML parser and well-formed XML input to follow normal parsing path (path B0->B2->B3)
        Parser parser = Parser.xmlParser();
        Reader reader = new StringReader("<tag attr='v'/>");
        String baseUri = "urn:xml";
        // WHEN parsing the XML input
        Document doc = parser.parseInput(reader, baseUri);
        // THEN the document should contain exactly one <tag> element with attribute attr="v"
        Element found = doc.selectFirst("tag");
        assertEquals(1, doc.select("tag").size(), "Expected one <tag> element parsed from XML");
        assertEquals("v", found.attr("attr"), "Expected attribute 'attr' to have value 'v'");
    }
}