package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_parseInput_1_Test {

    @Test
    @DisplayName("parseInput(Reader,null) with null baseUri throws IllegalArgumentException")
    public void test_TC11() {
        // GIVEN a HTML parser and a reader with null baseUri to trigger argument validation (B0→B2→B6)
        Parser parser = Parser.htmlParser();
        Reader input = new StringReader("<p>test</p>");
        String baseUri = null;
        // WHEN/THEN: expect IllegalArgumentException for null baseUri
        assertThrows(IllegalArgumentException.class, () -> {
            parser.parseInput(input, baseUri);
        });
    }

    @Test
    @DisplayName("parseInput(String,baseUri) propagates exception from a stubbed XmlTreeBuilder")
    public void test_TC12() {
        // GIVEN a stub XmlTreeBuilder that always fails inside parse (B0→B2→B6)
        class StubXmlBuilder extends XmlTreeBuilder {
            @Override
            public Document parse(Reader in, String uri, Parser p) {
                throw new RuntimeException("xml-fail");
            }
        }
        Parser parser = new Parser(new StubXmlBuilder());
        String html = "<root/>";
        String baseUri = "u";
        // WHEN/THEN: RuntimeException with message "xml-fail" should be propagated
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            parser.parseInput(html, baseUri);
        });
        assertEquals("xml-fail", ex.getMessage());
    }

    @Test
    @DisplayName("parseInput(String,baseUri) with trackErrors enabled records parse errors for malformed HTML")
    public void test_TC13() {
        // GIVEN a HTML parser with error tracking enabled (max 1), malformed HTML to hit parse errors (B0→B1→B2→B7→B8)
        Parser parser = Parser.htmlParser().setTrackErrors(1);
        String html = "<div><span>"; // unclosed tags cause parse error
        String baseUri = "u";
        // WHEN: parseInput is called
        Document doc = parser.parseInput(html, baseUri);
        // THEN: tracking remains enabled and exactly one error is recorded
        assertAll(
            () -> assertTrue(parser.isTrackErrors(), "Error tracking should be enabled"),
            () -> assertEquals(1, parser.getErrors().size(), "One parse error should be recorded")
        );
    }

    @Test
    @DisplayName("parseInput(Reader,baseUri) with trackErrors disabled records no parse errors for malformed HTML")
    public void test_TC14() {
        // GIVEN a HTML parser with error tracking disabled, malformed HTML to test no errors recorded (B0→B1→B2→B7→B9)
        Parser parser = Parser.htmlParser().setTrackErrors(0);
        Reader input = new StringReader("<ul><li>"); // unclosed tags
        String baseUri = "u";
        // WHEN: parseInput is called
        Document doc = parser.parseInput(input, baseUri);
        // THEN: tracking disabled and no errors in list
        assertAll(
            () -> assertFalse(parser.isTrackErrors(), "Error tracking should be disabled"),
            () -> assertTrue(parser.getErrors().isEmpty(), "No parse errors should be recorded")
        );
    }
}