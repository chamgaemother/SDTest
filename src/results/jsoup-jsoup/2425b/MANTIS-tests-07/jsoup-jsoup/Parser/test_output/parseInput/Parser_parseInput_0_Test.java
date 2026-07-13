package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.parser.HtmlTreeBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_parseInput_0_Test {

    @Test
    @DisplayName("parseInput(String, String) returns a Document with elements parsed for simple HTML")
    public void test_TC01_O1() {
        // Given a simple HTML paragraph input, we expect the parser to create corresponding <p> element
        String html = "<p>Hello</p>";
        String baseUri = "http://example.com";
        Parser parser = new Parser(new HtmlTreeBuilder());
        // When parsing via the String overload, path follows lock acquisition and parse
        Document doc = parser.parseInput(html, baseUri);
        // Then exactly one <p> element with text "Hello" should be present
        List<Element> paragraphs = doc.select("p");
        assertEquals(1, paragraphs.size(), "Expected one <p> element");
        assertEquals("Hello", paragraphs.get(0).text(), "Expected <p> text to be 'Hello'");
    }

    @Test
    @DisplayName("parseInput(String, String) with empty HTML returns an empty body Document")
    public void test_TC02_O1() {
        // Given empty HTML, parser should create a document with empty body
        String html = "";
        String baseUri = "http://example.com";
        Parser parser = new Parser(new HtmlTreeBuilder());
        // When parsing empty content
        Document doc = parser.parseInput(html, baseUri);
        // Then body should have no child elements
        assertTrue(doc.body().children().isEmpty(), "Expected body to have no children for empty HTML");
    }

    @Test
    @DisplayName("parseInput(String, String) with null html throws NullPointerException before parsing")
    public void test_TC03_O1() {
        // Given null HTML string, constructing StringReader should immediately throw NPE
        String html = null;
        String baseUri = "http://example.com";
        Parser parser = new Parser(new HtmlTreeBuilder());
        // When calling parseInput with null html, expect NPE
        assertThrows(NullPointerException.class, () -> parser.parseInput(html, baseUri));
    }

    @Test
    @DisplayName("parseInput(Reader, String) returns a Document with parsed elements for simple HTML")
    public void test_TC04_O2() {
        // Given a Reader over simple <div> text, parser should create a <div> element
        Reader reader = new StringReader("<div>DivText</div>");
        String baseUri = "http://example.com";
        Parser parser = new Parser(new HtmlTreeBuilder());
        // When parsing via Reader overload
        Document doc = parser.parseInput(reader, baseUri);
        // Then exactly one <div> element with text "DivText" should exist
        List<Element> divs = doc.select("div");
        assertEquals(1, divs.size(), "Expected one <div> element");
        assertEquals("DivText", divs.get(0).text(), "Expected <div> text to be 'DivText'");
    }

    @Test
    @DisplayName("parseInput(Reader, String) when Reader throws IOException wraps it into UncheckedIOException")
    public void test_TC05_O2() {
        // Given a Reader that always throws IOException on read, parser should wrap into UncheckedIOException
        Reader faultyReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("fail");
            }
            @Override
            public void close() throws IOException {
                // no-op
            }
        };
        String baseUri = "http://example.com";
        Parser parser = new Parser(new HtmlTreeBuilder());
        // When parsing, expect UncheckedIOException due to underlying IOException
        assertThrows(UncheckedIOException.class, () -> parser.parseInput(faultyReader, baseUri));
    }

    @Test
    @DisplayName("parseInput(Reader, String) with null Reader throws NullPointerException before locking")
    public void test_TC06_O2() {
        // Given null Reader, lock not yet acquired, calling parseInput should NPE
        Reader reader = null;
        String baseUri = "http://example.com";
        Parser parser = new Parser(new HtmlTreeBuilder());
        // Expect NPE immediately
        assertThrows(NullPointerException.class, () -> parser.parseInput(reader, baseUri));
    }
}