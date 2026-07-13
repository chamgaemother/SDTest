package org.jsoup.parser;

import org.jsoup.parser.Parser;
import org.jsoup.parser.HtmlTreeBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for Parser.parseInput methods based on provided scenarios TC05 and TC06.
 * Each test is self-contained to validate proper handling of null inputs.
 */
public class Parser_parseInput_1_Test {

    @Test
    @DisplayName("parseInput(String, baseUri) with null html argument throws NullPointerException in StringReader constructor")
    public void test_TC05() {
        // GIVEN a Parser instance and a null HTML string input
        Parser parser = new Parser(new HtmlTreeBuilder());
        String html = null;
        String baseUri = "http://example.com";
        // WHEN & THEN: invoking the String overload should immediately throw NullPointerException
        // Path B0->B1->B6: enters String overload (B1) and StringReader constructor throws NPE (B6)
        assertThrows(NullPointerException.class, () -> {
            parser.parseInput(html, baseUri);
        });
    }

    @Test
    @DisplayName("parseInput(Reader, baseUri) with null Reader throws NullPointerException before parse")
    public void test_TC06() {
        // GIVEN a Parser instance and a null Reader input
        Parser parser = new Parser(new HtmlTreeBuilder());
        Reader reader = null;
        String baseUri = "http://example.com";
        // WHEN & THEN: invoking the Reader overload should throw NullPointerException
        // Path B0->B1->B2->B6: enters Reader overload (B2) and using a null Reader should cause NPE before parsing
        assertThrows(NullPointerException.class, () -> {
            parser.parseInput(reader, baseUri);
        });
    }
}