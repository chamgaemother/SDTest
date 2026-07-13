package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * JUnit 5 test class for Parser.parseFragmentInput scenarios.
 */
public class Parser_parseFragmentInput_0_Test {

    @Test
    @DisplayName("TC01_O1: String overload with empty fragment and null context returns empty node list")
    public void test_TC01_O1() {
        // Given: empty fragment -> fragment.isEmpty() true, context null
        String fragment = "";
        Element context = null;
        String baseUri = "http://example.com";
        Parser parser = Parser.htmlParser();
        // When
        List<Node> result = parser.parseFragmentInput(fragment, context, baseUri);
        // Then: expect zero nodes for empty fragment
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("TC02_O1: String overload with single element fragment and non-null context returns one node")
    public void test_TC02_O1() {
        // Given: non-empty fragment, context non-null -> loop executes once
        String fragment = "<span>text</span>";
        Document doc = Document.createShell("http://x");
        Element context = doc.body();
        String baseUri = "http://x";
        Parser parser = Parser.htmlParser();
        // When
        List<Node> result = parser.parseFragmentInput(fragment, context, baseUri);
        // Then: one node with tagName span
        assertEquals(1, result.size());
        Node node = result.get(0);
        assertEquals("span", ((Element) node).tagName());
    }

    @Test
    @DisplayName("TC03_O2: Reader overload with multi-element fragment returns two nodes")
    public void test_TC03_O2() {
        // Given: Reader with two sibling elements -> loop runs twice, context null
        Reader fragment = new StringReader("<a></a><b></b>");
        Element context = null;
        String baseUri = "urn:test";
        Parser parser = new Parser(new HtmlTreeBuilder());
        // When
        List<Node> result = parser.parseFragmentInput(fragment, context, baseUri);
        // Then: two nodes with tagNames a and b
        assertEquals(2, result.size());
        assertEquals("a", ((Element) result.get(0)).tagName());
        assertEquals("b", ((Element) result.get(1)).tagName());
    }

    @Test
    @DisplayName("TC04_O1: String overload with null baseUri throws NullPointerException")
    public void test_TC04_O1() {
        // Given: fragment non-empty, baseUri null triggers NPE when resolving URIs
        String fragment = "<p></p>";
        Element context = null;
        String baseUri = null;
        Parser parser = Parser.htmlParser();
        // When & Then: expect NullPointerException due to null baseUri
        assertThrows(NullPointerException.class, () -> parser.parseFragmentInput(fragment, context, baseUri));
    }

    @Test
    @DisplayName("TC05_O2: Reader overload with Reader throwing IOException wraps in UncheckedIOException")
    public void test_TC05_O2() {
        // Given: Reader stub that throws IOException on read -> checked IOE wraps into UncheckedIOException
        Reader fragment = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("fail");
            }
            @Override
            public void close() throws IOException { /* no-op */ }
        };
        Element context = null;
        String baseUri = "x";
        Parser parser = new Parser(new HtmlTreeBuilder());
        // When & Then: expect UncheckedIOException wrapping the IOException
        assertThrows(UncheckedIOException.class, () -> parser.parseFragmentInput(fragment, context, baseUri));
    }
}