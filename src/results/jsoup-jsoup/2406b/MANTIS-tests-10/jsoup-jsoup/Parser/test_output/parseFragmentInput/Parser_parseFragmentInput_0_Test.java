package org.jsoup.parser;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.parser.ParseErrorList;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.TagSet;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseFragmentInput_0_Test {

    /**
     * Stub TreeBuilder that returns an empty list for parseFragment.
     */
    static class StubTreeBuilderEmpty extends org.jsoup.parser.TreeBuilder {
        StubTreeBuilderEmpty() { super(); }
        @Override public org.jsoup.parser.ParseSettings defaultSettings() { return new ParseSettings(ParseSettings.preserveCase); }
        @Override public TagSet defaultTagSet() { return TagSet.html(); }
        @Override public String defaultNamespace() { return ""; }
        @Override public void process(org.jsoup.parser.Token token) {} // Implemented the abstract method
        @Override public org.jsoup.nodes.Document parse(Reader r, String baseUri, Parser parser) { return null; }
        @Override public List<Node> parseFragment(Reader fragment, @Nullable Element context, String baseUri, Parser parser) {
            return Collections.emptyList();
        }
    }

    /**
     * Stub TreeBuilder that returns a singleton list for parseFragment.
     */
    static class StubTreeBuilderSingle extends org.jsoup.parser.TreeBuilder {
        private final Node node;
        StubTreeBuilderSingle(Node node) { super(); this.node = node; }
        @Override public org.jsoup.parser.ParseSettings defaultSettings() { return new ParseSettings(ParseSettings.preserveCase); }
        @Override public TagSet defaultTagSet() { return TagSet.html(); }
        @Override public String defaultNamespace() { return ""; }
        @Override public void process(org.jsoup.parser.Token token) {} // Implemented the abstract method
        @Override public org.jsoup.nodes.Document parse(Reader r, String baseUri, Parser parser) { return null; }
        @Override public List<Node> parseFragment(Reader fragment, @Nullable Element context, String baseUri, Parser parser) {
            return Collections.singletonList(node);
        }
    }

    /**
     * Stub TreeBuilder that fails by throwing UncheckedIOException in parseFragment.
     */
    static class StubTreeBuilderFail extends org.jsoup.parser.TreeBuilder {
        StubTreeBuilderFail() { super(); }
        @Override public org.jsoup.parser.ParseSettings defaultSettings() { return new ParseSettings(ParseSettings.preserveCase); }
        @Override public TagSet defaultTagSet() { return TagSet.html(); }
        @Override public String defaultNamespace() { return ""; }
        @Override public void process(org.jsoup.parser.Token token) {} // Implemented the abstract method
        @Override public org.jsoup.nodes.Document parse(Reader r, String baseUri, Parser parser) { return null; }
        @Override public List<Node> parseFragment(Reader fragment, @Nullable Element context, String baseUri, Parser parser) {
            throw new UncheckedIOException(new IOException("stub failure"));
        }
    }

    /**
     * A minimal Node stub for identity checks.
     */
    static class StubNode extends Node {
        StubNode() { super("stub"); } // Used a valid constructor for Node
        @Override public String nodeName() { return "stub"; }
        @Override public void outerHtmlTail(org.jsoup.internal.QuietAppendable appendable, org.jsoup.nodes.Document.OutputSettings outputSettings) {} // Implemented the abstract method
    }

    @Test
    @DisplayName("TC01_O1: parseFragmentInput(String, null context) returns empty list when TreeBuilder produces no nodes")
    void test_TC01_O1() {
        Parser parser = new Parser(new StubTreeBuilderEmpty());
        String fragment = "";
        Element context = null;
        String baseUri = "http://example.com";
        List<Node> result = parser.parseFragmentInput(fragment, context, baseUri);
        assertTrue(result.isEmpty(), "Expected empty list when no nodes produced");
    }

    @Test
    @DisplayName("TC02_O1: parseFragmentInput(String, non-null context) returns single stub node")
    void test_TC02_O1() {
        StubNode node = new StubNode();
        Parser parser = new Parser(new StubTreeBuilderSingle(node));
        String fragment = "<p>text</p>";
        Element context = new Element("div");
        String baseUri = "http://example.com";
        List<Node> result = parser.parseFragmentInput(fragment, context, baseUri);
        assertEquals(1, result.size(), "Expected one node in result");
        assertSame(node, result.get(0), "Expected the stub node instance");
    }

    @Test
    @DisplayName("TC03_O1: parseFragmentInput(String, context) propagates UncheckedIOException when TreeBuilder fails")
    void test_TC03_O1() {
        Parser parser = new Parser(new StubTreeBuilderFail());
        String fragment = "<tag>";
        Element context = null;
        String baseUri = "http://example.com";
        assertThrows(UncheckedIOException.class, () -> parser.parseFragmentInput(fragment, context, baseUri));
    }

    @Test
    @DisplayName("TC04_O2: parseFragmentInput(Reader, null context) returns multiple stub nodes")
    void test_TC04_O2() {
        StubNode n1 = new StubNode();
        StubNode n2 = new StubNode();
        StubNode n3 = new StubNode();
        List<Node> nodes = List.of(n1, n2, n3);
        Parser parser = new Parser(new StubTreeBuilderSingle(nodes.get(0)) {
            @Override public List<Node> parseFragment(Reader fragment, @Nullable Element context, String baseUri, Parser parser) {
                return nodes;
            }
        });
        Reader reader = new StringReader("<a/><b/><c/>);
        Element context = null;
        String baseUri = "http://example.com";
        List<Node> result = parser.parseFragmentInput(reader, context, baseUri);
        assertEquals(3, result.size(), "Expected three nodes");
        assertSame(nodes, result, "Expected the exact list returned by stub");
    }

    @Test
    @DisplayName("TC05_O2: parseFragmentInput(Reader, non-null context) propagates UncheckedIOException when Reader.read fails")
    void test_TC05_O2() {
        Reader failingReader = new Reader() {
            @Override public int read(char[] cbuf, int off, int len) throws IOException { throw new IOException("fail"); }
            @Override public void close() throws IOException {}
        };
        Parser parser = Parser.htmlParser();
        Element context = new Element("span");
        String baseUri = "http://example.com";
        assertThrows(UncheckedIOException.class,
            () -> parser.parseFragmentInput(failingReader, context, baseUri),
            "Expected UncheckedIOException when underlying Reader throws IOException");
    }

    @Test
    @DisplayName("TC06_O1: parseFragmentInput(String, non-null context, empty baseUri) returns single stub node")
    void test_TC06_O1() {
        StubNode node = new StubNode();
        Parser parser = new Parser(new StubTreeBuilderSingle(node));
        String fragment = "<div/>";
        Element context = new Element("p");
        String baseUri = "";
        List<Node> result = parser.parseFragmentInput(fragment, context, baseUri);
        assertEquals(1, result.size(), "Expected one node with empty baseUri");
        assertSame(node, result.get(0), "Expected the stub node instance");
    }
}