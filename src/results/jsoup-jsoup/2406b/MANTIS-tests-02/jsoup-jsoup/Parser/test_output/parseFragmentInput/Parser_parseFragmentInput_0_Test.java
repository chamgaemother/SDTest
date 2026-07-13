package org.jsoup.parser;

import org.jsoup.parser.Parser;
import org.jsoup.parser.TreeBuilder;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Tag;
import org.jsoup.nodes.Attributes;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.ParseErrorList;
import org.jsoup.parser.TagSet;
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
     * Helper stub TreeBuilder that returns a predefined list from parseFragment, and supplies minimal defaults.
     */
    private static class StubTreeBuilder extends TreeBuilder {
        private final List<Node> toReturn;
        public StubTreeBuilder(List<Node> toReturn) {
            this.toReturn = toReturn;
        }
        @Override
        public ParseSettings defaultSettings() {
            return new ParseSettings(false,true);
        }
        @Override
        public TagSet defaultTagSet() {
            return new TagSet(TagSet.Html());
        }
        @Override
        public TreeBuilder newInstance() {
            return new StubTreeBuilder(toReturn);
        }
        @Override
        public String defaultNamespace() {
            return Parser.NamespaceHtml;
        }
        @Override
        public Document parse(Reader input, String baseUri, Parser parser) {
            throw new UnsupportedOperationException("Not used in fragment tests");
        }
        @Override
        public List<Node> parseFragment(Reader input, Element context, String baseUri, Parser parser) {
            // ignore input and context, return stub list
            return toReturn;
        }
        @Override
        protected void initialiseParse(Reader r, String baseUri, Parser parser) {
            // no-op
        }
        @Override
        public void process(org.jsoup.parser.Token token) {
            // no-op
        }
    }

    @Test
    @DisplayName("String overload, non-null fragment and non-null context returns parsed nodes")
    void test_TC01_O1() {
        // design: non-null fragment/context triggers parseFragment path, stub returns [nodeA]
        Node nodeA = new Element(Tag.valueOf("p"), "base", new Attributes());
        StubTreeBuilder sb = new StubTreeBuilder(List.of(nodeA));
        Parser parser = new Parser(sb);
        String fragment = "<p>test</p>";
        Element ctx = new Element(Tag.valueOf("div"), "", new Attributes());
        String base = "base";
        List<Node> result = parser.parseFragmentInput(fragment, ctx, base); // B0→B1→B2→B3→B4
        assertEquals(List.of(nodeA), result);
    }

    @Test
    @DisplayName("String overload with null fragment throws NullPointerException at StringReader constructor")
    void test_TC02_O1() {
        // design: null fragment causes new StringReader(null) to NPE before parseFragment
        Parser parser = Parser.htmlParser();
        Element ctx = new Element(Tag.valueOf("div"), "", new Attributes());
        assertThrows(NullPointerException.class, () -> parser.parseFragmentInput((String) null, ctx, "base"));
    }

    @Test
    @DisplayName("String overload with empty fragment and null context returns empty list")
    void test_TC03_O1() {
        // design: empty input and null context still hits stub builder path, returns emptyList
        StubTreeBuilder sb = new StubTreeBuilder(Collections.emptyList());
        Parser parser = new Parser(sb);
        String fragment = "";
        Element ctx = null;
        String base = "base";
        List<Node> result = parser.parseFragmentInput(fragment, ctx, base); // B0→B1→B2→B3→B4
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Reader overload, non-null context returns parsed nodes")
    void test_TC04_O2() {
        // design: non-null context with Reader overload, stub returns [nodeB]
        Node nodeB = new Element(Tag.valueOf("span"), "base", new Attributes());
        StubTreeBuilder sb = new StubTreeBuilder(List.of(nodeB));
        Parser parser = new Parser(sb);
        Reader reader = new StringReader("<span>ok</span>");
        Element ctx = new Element(Tag.valueOf("span"), "", new Attributes());
        String base = "base";
        List<Node> result = parser.parseFragmentInput(reader, ctx, base); // B0→B1→B2→B3→B4
        assertEquals(List.of(nodeB), result);
    }

    @Test
    @DisplayName("Reader overload with null context returns parsed nodes")
    void test_TC05_O2() {
        // design: null context with Reader overload, stub returns [nodeC]
        Node nodeC = new Element(Tag.valueOf("br"), "base", new Attributes());
        StubTreeBuilder sb = new StubTreeBuilder(List.of(nodeC));
        Parser parser = new Parser(sb);
        Reader reader = new StringReader("<br>");
        Element ctx = null;
        String base = "base";
        List<Node> result = parser.parseFragmentInput(reader, ctx, base); // B0→B1→B2→B3→B4
        assertEquals(List.of(nodeC), result);
    }

    @Test
    @DisplayName("Reader overload when Reader throws IOException wraps into UncheckedIOException")
    void test_TC06_O2() {
        // design: Reader.read throws IOException to drive exception path, expecting UncheckedIOException
        Parser parser = Parser.htmlParser();
        Reader throwingReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("forced");
            }
            @Override
            public void close() throws IOException {
                // no-op
            }
        };
        Element ctx = new Element(Tag.valueOf("p"), "", new Attributes());
        assertThrows(UncheckedIOException.class, () -> parser.parseFragmentInput(throwingReader, ctx, "base"));
    }
}