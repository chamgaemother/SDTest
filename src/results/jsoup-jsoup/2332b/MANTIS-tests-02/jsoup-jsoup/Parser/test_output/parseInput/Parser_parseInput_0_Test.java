package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.parser.Parser;
import org.jsoup.parser.HtmlTreeBuilder;
import org.jsoup.nodes.Document;

import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_parseInput_0_Test {

    @Test
    @DisplayName("TC01_O1: parseInput(String, String) with valid HTML returns a non-null Document with correct baseUri")
    public void test_TC01_O1() {
        // GIVEN: valid HTML string and base URI
        String html = "<html><head></head><body><p>Hello</p></body></html>";
        String baseUri = "http://example.com";
        Parser parser = new Parser(new HtmlTreeBuilder());
        // WHEN: calling the String overload, should go B0→B1→B2→B3 (normal branch)
        Document d = parser.parseInput(html, baseUri);
        // THEN: result is non-null and baseUri is preserved
        assertNotNull(d, "Document should not be null for valid HTML input");
        assertEquals("http://example.com", d.baseUri(), "Base URI should match the provided value");
    }

    @Test
    @DisplayName("TC02_O2: parseInput(Reader, String) with valid Reader returns a non-null Document with correct baseUri")
    public void test_TC02_O2() {
        // GIVEN: valid Reader over HTML fragment and base URI
        Reader rdr = new StringReader("<div>Test</div>");
        String base = "https://foo.bar";
        Parser parser = new Parser(new HtmlTreeBuilder());
        // WHEN: calling the Reader overload, exercising B0→B1→B2→B3 (normal branch)
        Document d = parser.parseInput(rdr, base);
        // THEN: Document should be non-null and have the expected base URI
        assertNotNull(d, "Document should not be null when parsing from a Reader");
        assertEquals("https://foo.bar", d.baseUri(), "Base URI should be preserved from the Reader overload");
    }

    @Test
    @DisplayName("TC03_O1: parseInput(String, String) with null html argument throws NullPointerException")
    public void test_TC03_O1() {
        // GIVEN: null HTML string and a valid base URI
        String html = null;
        String baseUri = "http://x";
        Parser parser = new Parser(new HtmlTreeBuilder());
        // WHEN / THEN: constructing a StringReader(null) should cause NPE at overload entry B1→B4
        assertThrows(NullPointerException.class, () -> parser.parseInput(html, baseUri),
                "Passing null HTML string must throw NullPointerException");
    }

    @Test
    @DisplayName("TC04_O2: parseInput(Reader, String) with null Reader throws NullPointerException")
    public void test_TC04_O2() {
        // GIVEN: null Reader and a valid base URI
        Reader rdr = null;
        String baseUri = "http://y";
        Parser parser = new Parser(new HtmlTreeBuilder());
        // WHEN / THEN: calling the Reader overload with null should immediately throw NPE at B1→B4
        assertThrows(NullPointerException.class, () -> parser.parseInput(rdr, baseUri),
                "Passing null Reader must throw NullPointerException");
    }
}