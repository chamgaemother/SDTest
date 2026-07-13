package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_parseInput_1_Test {

    @Test
    @DisplayName("parseInput(String,String) with custom stub TreeBuilder returns stub Document (branch-custom-builder-return)")
    public void test_TC09() {
        // GIVEN a dummy Document and a stub TreeBuilder whose parse() always returns it
        Document dummy = new Document("http://x");
        TreeBuilder stub = new TreeBuilder() {
            @Override
            public Document parse(Reader in, String uri, Parser p) {
                // branch B3: custom builder return
                return dummy;
            }
            @Override public List<Node> parseFragment(Reader reader, Element context, String baseUri, Parser parser) { return null; }
            @Override public TreeBuilder newInstance() { return this; }
            @Override public TagSet defaultTagSet() { return TagSet.html(); }
            @Override public ParseSettings defaultSettings() { return ParseSettings.htmlDefault(); }
            @Override public String defaultNamespace() { return Parser.NamespaceHtml; }
            @Override public void process(Token token) { /* no-op */ }
        };
        Parser parser = new Parser(stub);
        String html = "<p>x</p>";
        String uri = "base";

        // WHEN invoking parseInput(String, String)
        Document result = parser.parseInput(html, uri);

        // THEN the same dummy instance is returned
        assertSame(dummy, result);
    }

    @Test
    @DisplayName("parseInput(String,String) with stub TreeBuilder.parse throwing RuntimeException propagates same exception (exception-branch)")
    public void test_TC10() {
        // GIVEN a stub TreeBuilder whose parse() throws IllegalStateException
        TreeBuilder stub = new TreeBuilder() {
            @Override
            public Document parse(Reader in, String uri, Parser p) {
                // branch B4: exception path on parse
                throw new IllegalStateException("fail-str");
            }
            @Override public List<Node> parseFragment(Reader reader, Element context, String baseUri, Parser parser) { return null; }
            @Override public TreeBuilder newInstance() { return this; }
            @Override public TagSet defaultTagSet() { return TagSet.html(); }
            @Override public ParseSettings defaultSettings() { return ParseSettings.htmlDefault(); }
            @Override public String defaultNamespace() { return Parser.NamespaceHtml; }
            @Override public void process(Token token) { /* no-op */ }
        };
        Parser parser = new Parser(stub);
        String html = "x";
        String uri = "u";

        // WHEN calling parseInput(String, String), THEN IllegalStateException propagates
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> parser.parseInput(html, uri));
        assertEquals("fail-str", ex.getMessage());
    }

    @Test
    @DisplayName("parseInput(Reader,String) with stub TreeBuilder.parse throwing RuntimeException propagates same exception (exception-branch)")
    public void test_TC11() {
        // GIVEN a stub TreeBuilder whose parse() throws IllegalStateException for Reader input
        TreeBuilder stub = new TreeBuilder() {
            @Override
            public Document parse(Reader in, String uri, Parser p) {
                // branch B4: exception path on parse
                throw new IllegalStateException("fail-rdr");
            }
            @Override public List<Node> parseFragment(Reader reader, Element context, String baseUri, Parser parser) { return null; }
            @Override public TreeBuilder newInstance() { return this; }
            @Override public TagSet defaultTagSet() { return TagSet.html(); }
            @Override public ParseSettings defaultSettings() { return ParseSettings.htmlDefault(); }
            @Override public String defaultNamespace() { return Parser.NamespaceHtml; }
            @Override public void process(Token token) { /* no-op */ }
        };
        Parser parser = new Parser(stub);
        Reader rdr = new StringReader("x");
        String uri = "u";

        // WHEN calling parseInput(Reader, String), THEN IllegalStateException propagates
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> parser.parseInput(rdr, uri));
        assertEquals("fail-rdr", ex.getMessage());
    }
}