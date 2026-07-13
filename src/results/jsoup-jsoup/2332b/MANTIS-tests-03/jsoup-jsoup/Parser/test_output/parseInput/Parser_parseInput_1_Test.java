package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseInput_1_Test {

    @Test
    @DisplayName("parseInput(Reader, String) with null Reader throws NullPointerException")
    public void test_TC06() {
        // Given: a parser with HtmlTreeBuilder and a null Reader
        Parser parser = Parser.htmlParser();
        Reader rdr = null;
        String base = "http://example.com";
        // When/Then: passing a null Reader should immediately trigger NPE (branch B2)
        assertThrows(NullPointerException.class, () -> parser.parseInput(rdr, base));
    }

    @Test
    @DisplayName("parseInput(Reader, String) with null baseUri throws IllegalArgumentException")
    public void test_TC07() {
        // Given: a parser with HtmlTreeBuilder and a valid Reader but null baseUri
        Parser parser = Parser.htmlParser();
        Reader rdr = new StringReader("<div/>"); // Fixed unclosed string literal
        String base = null;
        // When/Then: null baseUri should cause IllegalArgumentException mentioning 'baseUri' (branch B3)
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> parser.parseInput(rdr, base));
        assertTrue(ex.getMessage().contains("baseUri"), "Exception message should mention 'baseUri'.");
    }

    @Test
    @DisplayName("parseInput(Reader, String) wraps IOException from TreeBuilder.parse into RuntimeException")
    public void test_TC08() {
        // Given: a custom TreeBuilder that throws IOException during parse (forcing branch B4->B5)
        TreeBuilder throwingBuilder = new TreeBuilder() {
            @Override
            public Document parse(Reader reader, String baseUri, Parser parser) throws IOException {
                throw new IOException("fail");
            }

            @Override
            public java.util.List<org.jsoup.nodes.Node> parseFragment(Reader reader, org.jsoup.nodes.Element context, String baseUri, Parser parser) {
                return null;
            }

            @Override
            public TreeBuilder newInstance() {
                return new TreeBuilder() {};
            }

            @Override
            public ParseSettings defaultSettings() {
                return ParseSettings.defaultSettings();
            }

            @Override
            public void process(org.jsoup.parser.Token token) {
                // No operation for this test case
            }
        };
        Parser parser = new Parser(throwingBuilder);
        Reader rdr = new StringReader("x");
        String base = "uri";
        // When/Then: IOException should be caught and wrapped in RuntimeException
        RuntimeException rex = assertThrows(RuntimeException.class,
            () -> parser.parseInput(rdr, base));
        assertNotNull(rex.getCause(), "RuntimeException should have a cause.");
        assertTrue(rex.getCause() instanceof IOException,
            "The cause of the RuntimeException should be an IOException.");
    }
}