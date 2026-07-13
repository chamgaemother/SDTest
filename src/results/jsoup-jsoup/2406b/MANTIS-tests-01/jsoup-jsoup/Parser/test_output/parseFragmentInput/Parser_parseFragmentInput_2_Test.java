package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JUnit 5 tests for Parser.parseFragmentInput overloads.
 */
public class Parser_parseFragmentInput_2_Test {

    @Test
    @DisplayName("String overload with null context and non-empty fragment returns correct nodes list")
    public void test_TC10() {
        // Scenario TC10: call String overload with null context
        // Branches: B0 (entry) -> B1 (String overload) -> B2 (Reader creation) -> B4 (lock acquired) -> B5 (parseFragment path)
        Parser parser = Parser.htmlParser();
        String fragment = "<p>Test</p>";
        Element ctx = null;

        // When: parsing HTML fragment with null context
        List<Node> result = parser.parseFragmentInput(fragment, ctx, "baseUri");

        // Then: expect exactly one <p> element in the result
        assertEquals(1, result.size(), "Expected one node in the result list");
        Node node = result.get(0);
        assertEquals(true, node instanceof Element, "Expected node to be an Element");
        Element el = (Element) node;
        assertEquals("p", el.tagName(), "Expected tag name to be 'p'");
    }

    @Test
    @DisplayName("Reader overload using xmlParser returns a single XML element node")
    public void test_TC11() {
        // Scenario TC11: call Reader overload with xmlParser and null context
        // Branches: B0 (entry) -> B1 (Reader overload) -> B4 (lock acquired) -> B5 (parseFragment path)
        Parser parser = Parser.xmlParser();
        Reader reader = new StringReader("<foo/>\n");
        Element ctx = null;

        // When: parsing XML fragment with null context
        List<Node> result = parser.parseFragmentInput(reader, ctx, "");

        // Then: expect exactly one <foo> element in the result
        assertEquals(1, result.size(), "Expected one node in the result list");
        Node node = result.get(0);
        assertEquals(true, node instanceof Element, "Expected node to be an Element");
        Element el = (Element) node;
        assertEquals("foo", el.tagName(), "Expected tag name to be 'foo'");
    }
}