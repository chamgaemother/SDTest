package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.parser.TreeBuilder;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for Parser.parseInput overloads to ensure proper exception propagation
 * and that the internal lock is always released even when parse() throws.
 */
public class Parser_parseInput_1_Test {

    @Test
    @DisplayName("TC04: parseInput(Reader, baseUri) propagates a runtime exception from custom TreeBuilder.parse and still unlocks")
    public void test_TC04() {
        // Arrange a stub TreeBuilder that always throws IllegalStateException in parse(...)
        TreeBuilder stubBuilder = new TreeBuilder() {
            @Override
            public ParseSettings defaultSettings() {
                return new ParseSettings(ParseSettings.preserveCase); // Using a constructor with parameters
            }
            @Override
            public TreeBuilder newInstance() {
                return new TreeBuilder(); // Return a new instance of TreeBuilder
            }
            @Override
            public Document parse(Reader in, String baseUri, Parser parser) {
                // Trigger the exception path in parseInput; B3 exception branch
                throw new IllegalStateException("parse failure");
            }
            @Override
            public java.util.List<Node> parseFragment(Reader in, Element context, String baseUri, Parser parser) {
                return null;
            }
            @Override
            public void process(Token token) {
                // Implementing the abstract method
            }
            @Override
            public org.jsoup.parser.TagSet defaultTagSet() {
                return TagSet.Html();
            }
        };
        Parser parser = new Parser(stubBuilder);
        Reader reader = new StringReader("<ok/>\n");

        // Act & Assert: exception is propagated, and lock.unlock() in finally must be called
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
            parser.parseInput(reader, "uri")
        );
        assertEquals("parse failure", ex.getMessage());
        // Further call to parseInput should not deadlock if unlock occurred
        // use a different reader to verify lock is free
        Reader reader2 = new StringReader("<ok/>\n");
        // The subsequent call still throws, showing lock isn't held from first call
        assertThrows(IllegalStateException.class, () -> parser.parseInput(reader2, "uri"));
    }

    @Test
    @DisplayName("TC05: parseInput(String, baseUri) propagates a runtime exception from custom TreeBuilder.parse and still unlocks")
    public void test_TC05() {
        // Arrange a stub HtmlTreeBuilder that always throws IllegalArgumentException in parse(...)
        TreeBuilder stubBuilder = new HtmlTreeBuilder() {
            @Override
            public Document parse(Reader in, String baseUri, Parser parser) {
                // Trigger the exception path in String overload which delegates to Reader overload
                throw new IllegalArgumentException("string parse fail");
            }
            @Override
            public TreeBuilder newInstance() {
                return new HtmlTreeBuilder(); // Return a new instance of HtmlTreeBuilder
            }
            @Override
            public void process(Token token) {
                // Implementing the abstract method
            }
        };
        Parser parser = new Parser(stubBuilder);
        String html = "<div/>";
        String baseUri = "u";

        // Act & Assert: exception is propagated with correct message, and lock is released
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            parser.parseInput(html, baseUri)
        );
        assertEquals("string parse fail", ex.getMessage());
        // Confirm lock released by invoking again
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () ->
            parser.parseInput(html, baseUri)
        );
        assertEquals("string parse fail", ex2.getMessage());
    }
}