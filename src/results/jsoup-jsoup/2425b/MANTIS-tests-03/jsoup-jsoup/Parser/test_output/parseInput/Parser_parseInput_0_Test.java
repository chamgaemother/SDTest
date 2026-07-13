package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
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
    @DisplayName("parseInput(String, String) returns a Document for valid HTML and non-null baseUri")
    public void test_TC01_O1() {
        // Using htmlParser and valid HTML string to traverse normal parsing branches B0→B4
        String html = "<html><head><title>Hi</title></head><body><p>Test</p></body></html>";
        String baseUri = "http://example.com";
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput(html, baseUri);
        assertNotNull(doc, "Expected non-null Document");
        Element p = doc.select("p").first();
        assertNotNull(p, "Expected <p> element to be present after parsing");
        assertEquals("Test", p.text(), "Parsed <p> text should match input");
    }

    @Test
    @DisplayName("parseInput(Reader, String) returns a Document for valid Reader and non-null baseUri")
    public void test_TC02_O2() {
        // Providing StringReader to follow overload Reader starting at B0→B4
        Reader input = new StringReader("<html><body><div>Content</div></body></html>");
        String baseUri = "http://test";
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput(input, baseUri);
        assertNotNull(doc, "Expected non-null Document from Reader input");
        Element div = doc.body().select("div").first();
        assertNotNull(div, "Expected <div> in body after parsing");
        assertEquals("Content", div.text(), "Parsed <div> text should match input");
    }

    @Test
    @DisplayName("parseInput(Reader, String) throws UncheckedIOException when Reader.read() fails")
    public void test_TC03_O2() {
        // Stub Reader that throws IOException to hit exception path in parseInput Reader overload
        Reader faulty = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("fail");
            }
            @Override
            public void close() throws IOException {
                // no-op
            }
        };
        String baseUri = "http://err";
        Parser parser = Parser.htmlParser();
        UncheckedIOException ex = assertThrows(UncheckedIOException.class, () -> {
            parser.parseInput(faulty, baseUri);
        }, "Expected UncheckedIOException when underlying Reader fails");
        assertTrue(ex.getCause() instanceof IOException, "Cause should be the original IOException");
        assertEquals("fail", ex.getCause().getMessage(), "IOException message should be propagated");
    }

    @Test
    @DisplayName("parseInput(String, String) throws NullPointerException when baseUri is null")
    public void test_TC04_O1() {
        // Passing null baseUri to invoke NPE in parseInput(String, String) overload
        String html = "<p>Hi</p>";
        Parser parser = Parser.htmlParser();
        assertThrows(NullPointerException.class, () -> {
            parser.parseInput(html, null);
        }, "Expected NullPointerException when baseUri is null");
    }
}