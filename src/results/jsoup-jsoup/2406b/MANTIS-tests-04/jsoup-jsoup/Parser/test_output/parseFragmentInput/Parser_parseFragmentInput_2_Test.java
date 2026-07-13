package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_parseFragmentInput_2_Test {

    @Test
    @DisplayName("TC14: parseFragmentInput(String,Element,baseUri) with non-null context and empty fragment returns empty list")
    public void test_TC14() {
        // GIVEN a fresh HTML parser and an empty fragment string
        Parser parser = Parser.htmlParser();
        Element context = new Document("http://example.com").body();
        String fragment = "";
        String baseUri = "http://example.com";
        // WHEN parsing an empty fragment with non-null context
        List<Node> nodes = parser.parseFragmentInput(fragment, context, baseUri);
        // THEN the result list must be empty (path B0→B1→B2→B3→B4 corresponds to empty input branch)
        assertTrue(nodes.isEmpty(), "Expected empty list when parsing an empty fragment");
    }

    @Test
    @DisplayName("TC15: parseFragmentInput on xmlParser preserves uppercase tag names in XML fragment with null context")
    public void test_TC15() {
        // GIVEN a fresh XML parser and an uppercase-tag fragment, with null context to hit XML case-sensitivity path
        Parser parser = Parser.xmlParser();
        String fragment = "<TAG></TAG>";
        Element context = null;
        String baseUri = "http://xml";
        // WHEN parsing the fragment
        List<Node> nodes = parser.parseFragmentInput(fragment, context, baseUri);
        // THEN we should get exactly one Element node whose tagName remains 'TAG' (case preserved in XML)
        assertEquals(1, nodes.size(), "Expected exactly one node in the parsed fragment");
        Node first = nodes.get(0);
        assertTrue(first instanceof Element, "Expected the parsed node to be an Element");
        Element elem = (Element) first;
        assertEquals("TAG", elem.tagName(), "Expected XML parser to preserve uppercase tag names");
    }

    @Test
    @DisplayName("TC16: parseFragmentInput(Reader,Element,baseUri) with null Reader throws NullPointerException")
    public void test_TC16() {
        // GIVEN a fresh HTML parser and a null Reader input to exercise the NPE path
        Parser parser = Parser.htmlParser();
        Reader reader = null;
        Element context = new Document("http://example.com").body();
        String baseUri = "http://example.com";
        // WHEN/THEN calling parseFragmentInput with null Reader must throw NullPointerException
        assertThrows(NullPointerException.class, () -> {
            parser.parseFragmentInput(reader, context, baseUri);
        }, "Expected NullPointerException when the Reader argument is null");
    }
}