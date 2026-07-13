package org.jsoup.parser;

import org.jsoup.parser.Parser;
import org.jsoup.parser.TreeBuilder;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.ParseSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseInput_2_Test {

    /**
     * Custom unchecked exception to simulate TreeBuilder failures.
     */
    static class CustomRuntimeException extends RuntimeException {
        CustomRuntimeException(String message) {
            super(message);
        }
    }

    @Test
    @DisplayName("TC07 parseInput(String, String) propagates RuntimeException from TreeBuilder.parse")
    void test_TC07() {
        // GIVEN: a TreeBuilder that always throws on parse(Reader, baseUri, parser)
        TreeBuilder failing = new TreeBuilder() {
            @Override public ParseSettings defaultSettings() {
                // Changed to use default constructor for ParseSettings
                return new ParseSettings();
            }
            @Override public TreeBuilder newInstance() {
                // retain same instance to hit clone path
                return this;
            }
            @Override public Document parse(Reader in, String baseUri, Parser parser) {
                // simulate failure at B3→B5
                throw new CustomRuntimeException("fail-string");
            }
            @Override public List<Node> parseFragment(Reader in, Element context, String baseUri, Parser parser) {
                return null;
            }
            @Override public boolean isContentForTagData(String name) {
                return false;
            }
            @Override public String defaultNamespace() {
                return Parser.NamespaceHtml;
            }
            @Override public void process(org.jsoup.parser.Token token) {
                // Changed method signature to match superclass
            }
        };
        Parser parser = new Parser(failing);
        // WHEN/THEN: parseInput(String, String) should propagate the custom runtime exception
        CustomRuntimeException ex = assertThrows(
            CustomRuntimeException.class,
            () -> parser.parseInput("<div/>", "http://test"),
            "Expected parseInput to propagate CustomRuntimeException"
        );
        assertEquals("fail-string", ex.getMessage(), "Exception message should be propagated unchanged");
    }

    @Test
    @DisplayName("TC08 parseInput(Reader, String) propagates RuntimeException from TreeBuilder.parse")
    void test_TC08() {
        // GIVEN: a TreeBuilder that always throws on parse(Reader, baseUri, parser)
        TreeBuilder failing = new TreeBuilder() {
            @Override public ParseSettings defaultSettings() {
                // Changed to use default constructor for ParseSettings
                return new ParseSettings();
            }
            @Override public TreeBuilder newInstance() {
                return this;
            }
            @Override public Document parse(Reader in, String baseUri, Parser parser) {
                // simulate failure at B3→B5
                throw new CustomRuntimeException("fail-reader");
            }
            @Override public List<Node> parseFragment(Reader in, Element context, String baseUri, Parser parser) {
                return null;
            }
            @Override public boolean isContentForTagData(String name) {
                return false;
            }
            @Override public String defaultNamespace() {
                return Parser.NamespaceHtml;
            }
            @Override public void process(org.jsoup.parser.Token token) {
                // Changed method signature to match superclass
            }
        };
        Parser parser = new Parser(failing);
        Reader reader = new StringReader("<img/>);
        // Fixed the unclosed string literal issue by adding the missing quote
        Reader reader = new StringReader("<img/>\");
        // WHEN/THEN: parseInput(Reader, String) should propagate the custom runtime exception
        CustomRuntimeException ex = assertThrows(
            CustomRuntimeException.class,
            () -> parser.parseInput(reader, "base"),
            "Expected parseInput to propagate CustomRuntimeException"
        );
        assertEquals("fail-reader", ex.getMessage(), "Exception message should be propagated unchanged");
    }
}