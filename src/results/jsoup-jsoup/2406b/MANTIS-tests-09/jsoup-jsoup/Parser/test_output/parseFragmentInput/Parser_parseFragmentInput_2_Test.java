package org.jsoup.parser;

import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseFragmentInput_2_Test {

    @Test
    @DisplayName("String-overload: real HtmlTreeBuilder parses non-empty fragment with context and returns Element/TextNode list")
    public void test_TC19() {
        // Use htmlParser and span context to reach public parseFragmentInput(String, Element, String) overload
        Parser parser = Parser.htmlParser();
        Element ctx = new Element("span");
        String frag = "<b>hello</b>";
        String base = "http://example.com";
        // Executes B0→B1→B2→B3→B4 path in parseFragmentInput
        List<Node> nodes = parser.parseFragmentInput(frag, ctx, base);
        // Expect one Element <b>hello</b>
        assertEquals(1, nodes.size(), "Should parse a single <b> element");
        Node n = nodes.get(0);
        assertTrue(n instanceof Element, "Node should be an Element");
        Element e = (Element) n;
        assertEquals("b", e.tagName(), "Tag name must be 'b'");
        assertEquals("hello", e.text(), "Element text should be 'hello'");
    }

    @Test
    @DisplayName("Reader-overload: real HtmlTreeBuilder parses fragment reader without context into Comment/TextNode list")
    public void test_TC20() {
        // Using htmlParser and reader overload to parse comment followed by text
        Parser parser = Parser.htmlParser();
        Reader r = new StringReader("<!--cmt-->end");
        Element ctx = new Element("div"); // context provided, not null
        String base = "baseUri";
        // Executes public parseFragmentInput(Reader, Element, String)
        List<Node> nodes = parser.parseFragmentInput(r, ctx, base);
        assertEquals(2, nodes.size(), "Should parse two nodes: Comment and TextNode");
        // First should be Comment with data 'cmt'
        Node first = nodes.get(0);
        assertTrue(first instanceof Comment, "First node should be Comment");
        Comment c = (Comment) first;
        assertEquals("cmt", c.getData(), "Comment data should be 'cmt'");
        // Second should be TextNode with text 'end'
        Node second = nodes.get(1);
        assertTrue(second instanceof TextNode, "Second node should be TextNode");
        TextNode t = (TextNode) second;
        assertEquals("end", t.text(), "TextNode text should be 'end'");
    }

    @Test
    @DisplayName("Reader-overload: builder throws RuntimeException on first call then returns on second, verifying lock unlock in finally")
    public void test_TC21() {
        // Stub TreeBuilder that throws on first parseFragment call, then succeeds
        AtomicInteger count = new AtomicInteger(0);
        TreeBuilder stub = new TreeBuilder() {
            @Override public ParseSettings defaultSettings() { return new ParseSettings(true, true); }
            @Override public TreeBuilder newInstance() { return this; }
            @Override public String defaultNamespace() { return ""; }
            @Override public List<Node> parseFragment(Reader reader, Element context, String baseUri, Parser parser) {
                // First invocation throws, second returns empty list
                if (count.getAndIncrement() == 0) {
                    throw new IllegalStateException("fail first");
                }
                return Collections.emptyList();
            }
            @Override public org.jsoup.nodes.Document parse(Reader reader, String baseUri, Parser parser) { return null; }
            @Override public void process(org.jsoup.parser.Token token) { /* Implement logic here */ }
        };
        Parser parser = new Parser(stub);
        Reader r1 = new StringReader("x");
        Element ctx = new Element("p");
        String base = "u";
        // First call: exception path B2(ex) to finally unlock
        assertThrows(IllegalStateException.class,
            () -> parser.parseFragmentInput(r1, ctx, base),
            "First call should throw IllegalStateException");
        // Second call: ensure lock was unlocked and now returns empty list
        Reader r2 = new StringReader("x"); // new reader for second call
        List<Node> result = parser.parseFragmentInput(r2, ctx, base); // should not deadlock
        assertTrue(result.isEmpty(), "Second call should return empty list without deadlock");
    }
}