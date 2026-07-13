package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.parser.TreeBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
public class Parser_parseInput_2_Test {

    @Test
    @DisplayName("TC05: parseInput(Reader, String) propagates IllegalStateException when TreeBuilder.parse(Reader) throws")
    public void test_TC05() {
        Parser parser = Parser.htmlParser();
        TreeBuilder stub = new TreeBuilder() {
            @Override
            public Document parse(Reader in, String baseUri, Parser p) {
                process(null); // Call to process method to satisfy abstract method requirement
                throw new IllegalStateException("stub read fail");
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
            public Document parse(String html, String baseUri, Parser p) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<Node> parseFragment(Reader in, org.jsoup.nodes.Element context, String baseUri, Parser p) {
                throw new UnsupportedOperationException();
            }

            @Override
            protected void process(org.jsoup.parser.Token token) {
                // Implement required logic for process method
            }
        };
        parser.setTreeBuilder(stub);

        Reader reader = new StringReader("irrelevant");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> parser.parseInput(reader, "http://base"));
        assert ex.getMessage().equals("stub read fail");
    }

    @Test
    @DisplayName("TC06: parseInput(String, String) propagates IllegalStateException when TreeBuilder.parse(Reader) throws via string overload")
    public void test_TC06() {
        Parser parser = Parser.htmlParser();
        TreeBuilder stub = new TreeBuilder() {
            @Override
            public Document parse(Reader in, String baseUri, Parser p) {
                process(null); // Call to process method to satisfy abstract method requirement
                throw new IllegalStateException("stub string fail");
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
            public Document parse(String html, String baseUri, Parser p) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<Node> parseFragment(Reader in, org.jsoup.nodes.Element context, String baseUri, Parser p) {
                throw new UnsupportedOperationException();
            }

            @Override
            protected void process(org.jsoup.parser.Token token) {
                // Implement required logic for process method
            }
        };
        parser.setTreeBuilder(stub);

        String html = "<p>x</p>";
        String base = "http://base";

        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> parser.parseInput(html, base));
        assert ex.getMessage().equals("stub string fail");
    }
}