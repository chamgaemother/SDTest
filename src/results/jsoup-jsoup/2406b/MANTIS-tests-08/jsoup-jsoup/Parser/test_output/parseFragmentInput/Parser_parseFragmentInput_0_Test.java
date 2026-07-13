package org.jsoup.parser;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseFragmentInput_0_Test {

    /**
     * A TreeBuilder stub that returns a predefined list of nodes and optionally records the baseUri passed in.
     */
    private static class StubTreeBuilder extends TreeBuilder {
        private final List<Node> nodesToReturn;
        private String capturedBaseUri;

        StubTreeBuilder(List<Node> nodes) {
            this.nodesToReturn = nodes;
        }

        @Override
        public List<Node> parseFragment(Reader reader, Element context, String baseUri, Parser parser) {
            // Directly return the stub list and record the baseUri for verification
            this.capturedBaseUri = baseUri;
            return nodesToReturn;
        }

        public String getCapturedBaseUri() {
            return capturedBaseUri;
        }

        @Override public ParseSettings defaultSettings() { return new ParseSettings(true, true); }
        @Override public TreeBuilder newInstance() { return new StubTreeBuilder(nodesToReturn); }
        @Override public Document parse(Reader input, String baseUri, Parser parser) { throw new UnsupportedOperationException(); }
        @Override public TagSet defaultTagSet() { return new TagSet(); }
        @Override public String defaultNamespace() { return ""; }
        @Override public void initialiseParse(Reader reader, String baseUri, Parser parser) {}

        @Override
        public void process(org.jsoup.parser.Token token) { /* Implement the method logic here */ }
    }

    @Test
    @DisplayName("String overload: non-empty fragment with non-null context returns expected nodes list")
    void test_TC01_O1() {
        // Using stub TreeBuilder returning exactly two distinct nodes
        Element node1 = new Element("a");
        Element node2 = new Element("b");
        List<Node> stubNodes = new ArrayList<>(); stubNodes.add(node1); stubNodes.add(node2);
        StubTreeBuilder stubBuilder = new StubTreeBuilder(stubNodes);
        Parser parser = new Parser(stubBuilder);
        String fragment = "<a></a><b></b>";
        Element ctx = new Element("div");
        String baseUri = "http://test";
        // call the String overload, non-null context
        List<Node> result = parser.parseFragmentInput(fragment, ctx, baseUri);
        // verify stub returned exactly the two nodes in order
        assertEquals(2, result.size(), "Expected two nodes returned");
        assertSame(node1, result.get(0), "First element should be stub node1");
        assertSame(node2, result.get(1), "Second element should be stub node2");
    }

    @Test
    @DisplayName("String overload: non-empty fragment with null context returns expected nodes list")
    void test_TC02_O1() {
        // Stub TreeBuilder returns one node when context is null
        Element node = new Element("p");
        List<Node> stubNodes = Collections.singletonList(node);
        StubTreeBuilder stubBuilder = new StubTreeBuilder(stubNodes);
        Parser parser = new Parser(stubBuilder);
        String fragment = "<p/>";
        Element ctx = null;
        String baseUri = "uri";
        // call overload with null context
        List<Node> result = parser.parseFragmentInput(fragment, ctx, baseUri);
        assertEquals(1, result.size(), "Expected one node returned");
        assertSame(node, result.get(0), "Returned node should match stub node");
    }

    @Test
    @DisplayName("String overload: empty fragment returns empty list")
    void test_TC03_O1() {
        // Stub returns empty list for empty input
        StubTreeBuilder stubBuilder = new StubTreeBuilder(Collections.emptyList());
        Parser parser = new Parser(stubBuilder);
        String fragment = "";
        Element ctx = new Element("span");
        String baseUri = "u";
        List<Node> result = parser.parseFragmentInput(fragment, ctx, baseUri);
        assertTrue(result.isEmpty(), "Expected empty list for empty fragment");
    }

    @Test
    @DisplayName("String overload: null baseUri passed to parseFragment")
    void test_TC04_O1() {
        // Stub records baseUri; test passing null
        Element node = new Element("x");
        List<Node> stubNodes = Collections.singletonList(node);
        StubTreeBuilder stubBuilder = new StubTreeBuilder(stubNodes);
        Parser parser = new Parser(stubBuilder);
        String fragment = "<x/>";
        Element ctx = new Element("x");
        String baseUri = null;
        List<Node> result = parser.parseFragmentInput(fragment, ctx, baseUri);
        // verify stub received null baseUri
        assertNull(stubBuilder.getCapturedBaseUri(), "Expected captured baseUri to be null");
        assertEquals(stubNodes, result, "Returned list should match stub output");
    }

    @Test
    @DisplayName("String overload: underlying Reader throws IOException leading to UncheckedIOException")
    void test_TC05_O1() {
        // Use a Reader stub throwing IOException on read
        Parser parser = Parser.htmlParser(); // real TreeBuilder for HTML
        Reader badReader = new Reader() {
            @Override public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("read error");
            }
            @Override public void close() {}
        };
        Element ctx = new Element("x");
        String baseUri = "u";
        // Expect UncheckedIOException wrapping the IOException
        UncheckedIOException ex = assertThrows(UncheckedIOException.class,
            () -> parser.parseFragmentInput(badReader, ctx, baseUri));
        assertEquals("read error", ex.getCause().getMessage());
    }

    @Test
    @DisplayName("Reader overload: non-empty fragment with non-null context returns expected nodes list")
    void test_TC06_O2() {
        // Stub returns three nodes for reader overload
        Element n1 = new Element("e1");
        Element n2 = new Element("e2");
        Element n3 = new Element("e3");
        List<Node> stubNodes = new ArrayList<>(); stubNodes.add(n1); stubNodes.add(n2); stubNodes.add(n3);
        StubTreeBuilder stubBuilder = new StubTreeBuilder(stubNodes);
        Parser parser = new Parser(stubBuilder);
        Reader reader = new StringReader("AAA");
        Element ctx = new Element("e");
        String baseUri = "b";
        List<Node> result = parser.parseFragmentInput(reader, ctx, baseUri);
        assertEquals(3, result.size(), "Expected three nodes returned");
        assertSame(n1, result.get(0));
        assertSame(n2, result.get(1));
        assertSame(n3, result.get(2));
    }

    @Test
    @DisplayName("Reader overload: non-empty fragment with null context returns expected nodes list")
    void test_TC07_O2() {
        // Stub returns empty list even for non-empty input
        StubTreeBuilder stubBuilder = new StubTreeBuilder(Collections.emptyList());
        Parser parser = new Parser(stubBuilder);
        Reader reader = new StringReader("<p>");
        Element ctx = null;
        String baseUri = "u";
        List<Node> result = parser.parseFragmentInput(reader, ctx, baseUri);
        assertTrue(result.isEmpty(), "Expected empty list when stub returns no nodes");
    }

    @Test
    @DisplayName("Reader overload: empty fragment returns empty list")
    void test_TC08_O2() {
        // Stub returns empty list for any input
        StubTreeBuilder stubBuilder = new StubTreeBuilder(Collections.emptyList());
        Parser parser = new Parser(stubBuilder);
        Reader reader = new StringReader("");
        Element ctx = new Element("e");
        String baseUri = "b";
        List<Node> result = parser.parseFragmentInput(reader, ctx, baseUri);
        assertTrue(result.isEmpty(), "Expected empty list for empty fragment");
    }

    @Test
    @DisplayName("Reader overload: null baseUri passed through to stub")
    void test_TC09_O2() {
        // Stub records null baseUri
        Element node = new Element("x");
        List<Node> stubNodes = Collections.singletonList(node);
        StubTreeBuilder stubBuilder = new StubTreeBuilder(stubNodes);
        Parser parser = new Parser(stubBuilder);
        Reader reader = new StringReader("<x>");
        Element ctx = new Element("x");
        String baseUri = null;
        List<Node> result = parser.parseFragmentInput(reader, ctx, baseUri);
        assertNull(stubBuilder.getCapturedBaseUri(), "Expected stub to capture null baseUri");
        assertEquals(stubNodes, result, "Returned list should match stub output");
    }

    @Test
    @DisplayName("Reader overload: underlying Reader throws IOException leading to UncheckedIOException")
    void test_TC10_O2() {
        // Reader stub throws IOException immediately
        Parser parser = Parser.xmlParser(); // real XML TreeBuilder
        Reader badReader = new Reader() {
            @Override public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("io fail");
            }
            @Override public void close() {}
        };
        Element ctx = new Element("e");
        String baseUri = "b";
        UncheckedIOException ex = assertThrows(UncheckedIOException.class,
            () -> parser.parseFragmentInput(badReader, ctx, baseUri));
        assertEquals("io fail", ex.getCause().getMessage());
    }
}