package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_parseInput_2_Test {

    @Test
    @DisplayName("TC09: parseInput(String, String) with custom stub TreeBuilder returns stub Document (branch-custom-builder-return)")
    void test_TC09() {
        // Inline stub TreeBuilder: parse returns dummy Document, exercising B0->B1->B2->B3
        Document dummy = new Document("http://x");
        TreeBuilder stubBuilder = new TreeBuilder() {
            @Override
            public Document parse(Reader in, String uri, Parser p) {
                // Always return our dummy to satisfy custom builder return path
                return dummy;
            }

            @Override
            public List<Node> parseFragment(Reader input, org.jsoup.nodes.Element context, String baseUri, Parser p) {
                throw new UnsupportedOperationException();
            }

            @Override
            public TreeBuilder newInstance() {
                return this;
            }

            @Override
            public ParseSettings defaultSettings() {
                return null;
            }

            @Override
            public String defaultNamespace() {
                return "";
            }

            @Override
            public TagSet defaultTagSet() {
                return TagSet.Html();
            }

            @Override
            public void process(org.jsoup.parser.Token token) {
                // Implementing the missing method
            }
        };
        Parser parser = new Parser(stubBuilder);
        String html = "<p>x</p>";
        String uri = "base";
        // WHEN: calling string overload
        Document result = parser.parseInput(html, uri);
        // THEN: must return the exact dummy instance
        assertSame(dummy, result);
    }

    @Test
    @DisplayName("TC10: parseInput(String, String) with stub TreeBuilder.parse throwing RuntimeException propagates exception (branch-custom-builder-exception)")
    void test_TC10() {
        // Stub TreeBuilder: parse throws IllegalStateException, to follow exception branch B4
        TreeBuilder stubBuilder = new TreeBuilder() {
            @Override
            public Document parse(Reader in, String uri, Parser p) {
                throw new IllegalStateException("fail-str");
            }

            @Override
            public List<Node> parseFragment(Reader input, org.jsoup.nodes.Element context, String baseUri, Parser p) {
                throw new UnsupportedOperationException();
            }

            @Override
            public TreeBuilder newInstance() {
                return this;
            }

            @Override
            public ParseSettings defaultSettings() {
                return null;
            }

            @Override
            public String defaultNamespace() {
                return "";
            }

            @Override
            public TagSet defaultTagSet() {
                return TagSet.Html();
            }

            @Override
            public void process(org.jsoup.parser.Token token) {
                // Implementing the missing method
            }
        };
        Parser parser = new Parser(stubBuilder);
        String html = "x";
        String uri = "u";
        // WHEN & THEN: expect IllegalStateException from stub parse
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> parser.parseInput(html, uri));
        assertEquals("fail-str", ex.getMessage());
    }

    @Test
    @DisplayName("TC11: parseInput(Reader, String) with custom stub TreeBuilder returns stub Document (branch-custom-builder-return)")
    void test_TC11() {
        // Stub builder returns dummy on Reader overload path B0->B1->B2->B3
        Document dummy = new Document("http://y");
        TreeBuilder stubBuilder = new TreeBuilder() {
            @Override
            public Document parse(Reader in, String uri, Parser p) {
                return dummy;
            }

            @Override
            public List<Node> parseFragment(Reader input, org.jsoup.nodes.Element context, String baseUri, Parser p) {
                throw new UnsupportedOperationException();
            }

            @Override
            public TreeBuilder newInstance() {
                return this;
            }

            @Override
            public ParseSettings defaultSettings() {
                return null;
            }

            @Override
            public String defaultNamespace() {
                return "";
            }

            @Override
            public TagSet defaultTagSet() {
                return TagSet.Html();
            }

            @Override
            public void process(org.jsoup.parser.Token token) {
                // Implementing the missing method
            }
        };
        Parser parser = new Parser(stubBuilder);
        Reader reader = new StringReader("<p>y</p>");
        String uri = "base";
        // WHEN: calling Reader overload
        Document result = parser.parseInput(reader, uri);
        // THEN: should return stub dummy
        assertSame(dummy, result);
    }

    @Test
    @DisplayName("TC12: parseInput(Reader, String) with stub TreeBuilder.parse throwing RuntimeException propagates exception (branch-custom-builder-exception)")
    void test_TC12() {
        // Stub builder parse throws on Reader overload to hit exception branch B4
        TreeBuilder stubBuilder = new TreeBuilder() {
            @Override
            public Document parse(Reader in, String uri, Parser p) {
                throw new IllegalStateException("fail-rdr");
            }

            @Override
            public List<Node> parseFragment(Reader input, org.jsoup.nodes.Element context, String baseUri, Parser p) {
                throw new UnsupportedOperationException();
            }

            @Override
            public TreeBuilder newInstance() {
                return this;
            }

            @Override
            public ParseSettings defaultSettings() {
                return null;
            }

            @Override
            public String defaultNamespace() {
                return "";
            }

            @Override
            public TagSet defaultTagSet() {
                return TagSet.Html();
            }

            @Override
            public void process(org.jsoup.parser.Token token) {
                // Implementing the missing method
            }
        };
        Parser parser = new Parser(stubBuilder);
        Reader reader = new StringReader("x");
        String uri = "u";
        // WHEN & THEN: verify exception propagates with correct message
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> parser.parseInput(reader, uri));
        assertEquals("fail-rdr", ex.getMessage());
    }
}