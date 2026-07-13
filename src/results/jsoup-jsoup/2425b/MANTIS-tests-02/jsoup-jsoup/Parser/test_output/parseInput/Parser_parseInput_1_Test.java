package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JUnit 5 tests for Parser.parseInput methods, covering both String and Reader inputs.
 */
public class Parser_parseInput_1_Test {

    @Test
    @DisplayName("TC04: parseInput(String, String) propagates NullPointerException when html argument is null")
    public void test_TC04() {
        // Scenario: html is null, so new StringReader(html) should NPE before parse is invoked
        Parser parser = Parser.htmlParser();
        String baseUri = "http://example.com";
        // Expect a NullPointerException when html=null
        assertThrows(NullPointerException.class, () -> {
            parser.parseInput((String) null, baseUri);
        });
    }

    @Test
    @DisplayName("TC05: parseInput(Reader, String) propagates custom RuntimeException from TreeBuilder.parse")
    public void test_TC05() {
        // Scenario: stub TreeBuilder throws RuntimeException("boom") in parse
        TreeBuilder stubBuilder = new TreeBuilder() {
            @Override
            public ParseSettings defaultSettings() {
                return new ParseSettings(ParseSettings.preserveCase, ParseSettings.preserveWhitespace);
            }

            @Override
            public TreeBuilder newInstance() {
                return this;
            }

            @Override
            public String defaultNamespace() {
                return Parser.NamespaceHtml;
            }

            @Override
            public Document parse(Reader reader, String baseUri, Parser parser) {
                // direct throw to simulate internal failure
                throw new RuntimeException("boom");
            }

            @Override
            public List<Node> parseFragment(Reader reader, Element context, String baseUri, Parser parser) {
                return null;
            }

            @Override
            public void process(Token token) {
                // Implementing the abstract method from TreeBuilder
                // No operation needed for this test
            }
        };
        Parser parser = new Parser(stubBuilder);
        Reader reader = new StringReader("<p>fail</p>");
        String baseUri = "http://x/";
        // Expect the stub's RuntimeException with message "boom"
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            parser.parseInput(reader, baseUri);
        });
        assertEquals("boom", ex.getMessage());
    }
}