package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
public class Parser_parseInput_1_Test {

    @Test
    @DisplayName("parseInput(Reader, String) propagates exception when TreeBuilder.parse throws IllegalStateException")
    public void test_TC05() {
        // Arrange a Parser with a TreeBuilder stub that throws IllegalStateException in parse(Reader, baseUri, parser)
        Parser parser = Parser.htmlParser();
        TreeBuilder stub = new TreeBuilder() {
            @Override
            public Document parse(Reader in, String uri, Parser p) {
                // stub to trigger B3->B5 exception propagation
                throw new IllegalStateException("stub fail");
            }

            @Override
            public org.jsoup.parser.ParseSettings defaultSettings() {
                return parser.settings();
            }

            @Override
            public TreeBuilder newInstance() {
                return this;
            }

            @Override
            public Document parse(String html, String baseUri, Parser parser) {
                throw new UnsupportedOperationException();
            }

            @Override
            public java.util.List<org.jsoup.nodes.Node> parseFragment(Reader in, org.jsoup.nodes.Element context, String uri, Parser p) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void process(org.jsoup.parser.Token token) {
                // no-op
            }
        };
        parser.setTreeBuilder(stub);
        Reader reader = new StringReader("irrelevant");

        // Act & Assert: expect IllegalStateException with stub message
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> parser.parseInput(reader, "http://base")
        );
        assertEquals("stub fail", ex.getMessage());
    }

    @Test
    @DisplayName("parseInput(String, String) propagates exception when TreeBuilder.parse throws IllegalStateException")
    public void test_TC06() {
        // Arrange a Parser with a TreeBuilder stub that throws IllegalStateException in parse(Reader, baseUri, parser)
        Parser parser = Parser.htmlParser();
        TreeBuilder stub = new TreeBuilder() {
            @Override
            public Document parse(Reader in, String uri, Parser p) {
                // stub to trigger B1->B3->B5 via string overload path
                throw new IllegalStateException("stub fail string");
            }

            @Override
            public org.jsoup.parser.ParseSettings defaultSettings() {
                return parser.settings();
            }

            @Override
            public TreeBuilder newInstance() {
                return this;
            }

            @Override
            public Document parse(String html, String baseUri, Parser parser) {
                throw new UnsupportedOperationException();
            }

            @Override
            public java.util.List<org.jsoup.nodes.Node> parseFragment(Reader in, org.jsoup.nodes.Element context, String uri, Parser p) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void process(org.jsoup.parser.Token token) {
                // no-op
            }
        };
        parser.setTreeBuilder(stub);
        String html = "<p>x</p>";
        String base = "http://base";

        // Act & Assert: expect IllegalStateException with stub message from string overload
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> parser.parseInput(html, base)
        );
        assertEquals("stub fail string", ex.getMessage());
    }
}