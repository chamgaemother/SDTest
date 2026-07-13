package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.parser.ParseErrorList;
import org.jsoup.parser.HtmlTreeBuilder;
import java.io.Reader;
import java.io.StringReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
public class Parser_parseFragmentInput_0_Test {

    @Test
    @DisplayName("parseFragmentInput(String, null, baseUri) on empty fragment returns empty list (loop-0)")
    public void test_TC01() {
        // Context is null to take branch where context==null
        String fragment = "";
        Element context = null;
        String baseUri = "http://example.com";
        Parser parser = Parser.htmlParser();
        // invoking overload that wraps to Reader
        List<Node> nodes = parser.parseFragmentInput(fragment, context, baseUri);
        // expect empty list when no tags to parse
        assertEquals(0, nodes.size(), "Empty fragment should yield zero nodes");
    }

    @Test
    @DisplayName("parseFragmentInput(String, null, baseUri) on single element returns one node (loop-1)")
    public void test_TC02() {
        // Context null branch; single element yields one node
        String fragment = "<p></p>";
        Element context = null;
        String baseUri = "http://example.com";
        Parser parser = Parser.htmlParser();
        List<Node> nodes = parser.parseFragmentInput(fragment, context, baseUri);
        assertEquals(1, nodes.size(), "Single element fragment should yield one node");
        assertTrue(nodes.get(0) instanceof Element, "Node should be Element");
    }

    @Test
    @DisplayName("parseFragmentInput(String, null, baseUri) on multiple siblings returns two nodes (loop-N)")
    public void test_TC03() {
        // Context null branch; two sibling elements parsed
        String fragment = "<p></p><div></div>";
        Element context = null;
        String baseUri = "http://example.com";
        Parser parser = Parser.htmlParser();
        List<Node> nodes = parser.parseFragmentInput(fragment, context, baseUri);
        assertEquals(2, nodes.size(), "Two sibling elements should yield two nodes");
        assertTrue(nodes.get(0) instanceof Element, "First node should be Element");
        assertTrue(nodes.get(1) instanceof Element, "Second node should be Element");
    }

    @Test
    @DisplayName("parseFragmentInput(Reader, non-null context, baseUri) returns nodes without mutating context (loop-1)")
    public void test_TC04() {
        // Non-null context to take branch where context!=null
        Reader fragmentReader = new StringReader("<span>hi</span>");
        // using Element via HtmlTreeBuilder default context
        Element context = new Element(org.jsoup.parser.Tag.valueOf("div"), "http://example.com");
        String baseUri = "http://example.com";
        Parser parser = new Parser(new HtmlTreeBuilder());
        List<Node> nodes = parser.parseFragmentInput(fragmentReader, context, baseUri);
        // ensure one node parsed and original context not mutated
        assertEquals(1, nodes.size(), "Single span element yields one node");
        assertEquals(0, context.childNodeSize(), "Context children should remain unchanged");
    }

    @Test
    @DisplayName("parseFragmentInput(Reader, context, baseUri) propagates UncheckedIOException on reader error")
    public void test_TC05() {
        // Create a Reader that throws IOException to simulate I/O error
        Reader badReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("Read failed");
            }
            @Override
            public void close() throws IOException {
                // no-op
            }
        };
        Element context = new Element(org.jsoup.parser.Tag.valueOf("div"), "http://example.com");
        String baseUri = "http://example.com";
        Parser parser = new Parser(new HtmlTreeBuilder());
        // Expect UncheckedIOException wrapping the underlying IOException
        UncheckedIOException thrown = assertThrows(UncheckedIOException.class, () -> {
            parser.parseFragmentInput(badReader, context, baseUri);
        }, "Should propagate IO error as UncheckedIOException");
        assertEquals("java.io.IOException: Read failed", thrown.getCause().toString());
    }
}