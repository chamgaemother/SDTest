package org.jsoup.parser;

import org.jsoup.parser.Parser;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_parseFragmentInput_1_Test {

    @Test
    @DisplayName("TC11: Reader overload with xmlParser parses self-closing tag in XML context")
    public void test_TC11() {
        // Use xmlParser to enter XML branch (path B3->B4->B7->B8), self-closing <a/>
        Reader fragment = new StringReader("<a/>\n");
        Parser parser = Parser.xmlParser();
        List<Node> result = parser.parseFragmentInput(fragment, null, "base");
        // Expect one element named 'a'
        assertEquals(1, result.size(), "Should parse exactly one node");
        Node node = result.get(0);
        assertTrue(node instanceof Element, "Parsed node should be an Element");
        Element el = (Element) node;
        assertEquals("a", el.tagName(), "Tag name should be 'a'");
    }

    @Test
    @DisplayName("TC12: String overload with xmlParser parses mismatched case tag in XML context")
    public void test_TC12() {
        // Use xmlParser and string overload to hit branch B2 then B3->B4->B7->B8, tag <Test/>
        String fragment = "<Test/>";
        Parser parser = Parser.xmlParser();
        List<Node> result = parser.parseFragmentInput(fragment, null, "base");
        // Expect one element named 'Test' preserving case
        assertEquals(1, result.size(), "Should parse exactly one node");
        Node node = result.get(0);
        assertTrue(node instanceof Element, "Parsed node should be an Element");
        Element el = (Element) node;
        assertEquals("Test", el.tagName(), "Tag name should preserve case 'Test'");
    }

    @Test
    @DisplayName("TC13: Reader overload throws NullPointerException when fragment reader is null")
    public void test_TC13() {
        // Calling reader overload with null Reader should throw NullPointerException before parsing begins
        Parser parser = Parser.htmlParser();
        assertThrows(NullPointerException.class, () -> {
            parser.parseFragmentInput((Reader) null, null, "base");
        }, "Passing null Reader should cause NullPointerException");
    }

    @Test
    @DisplayName("TC14: String overload throws NullPointerException when fragment string is null")
    public void test_TC14() {
        // String overload constructs new StringReader(fragment), passing null should NPE at construction
        Parser parser = Parser.htmlParser();
        assertThrows(NullPointerException.class, () -> {
            parser.parseFragmentInput((String) null, null, "base");
        }, "Passing null String should cause NullPointerException");
    }

    @Test
    @DisplayName("TC15: Reader overload throws NullPointerException when baseUri is null")
    public void test_TC15() {
        // Reader overload with valid Reader but null baseUri should throw NPE in parseFragment
        Reader fragment = new StringReader("<p>x</p>");
        Parser parser = Parser.htmlParser();
        assertThrows(NullPointerException.class, () -> {
            parser.parseFragmentInput(fragment, null, null);
        }, "Passing null baseUri should cause NullPointerException");
    }
}