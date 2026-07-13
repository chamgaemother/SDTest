package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_parseInput_0_Test {

    @Test
    @DisplayName("parseInput(String, String) with non-empty HTML and valid baseUri returns a Document via treeBuilder.parse")
    public void test_TC01_O1() {
        // Given a simple <p>Test</p> HTML, expecting branch through lock, parse call, and normal return
        String html = "<p>Test</p>";
        String baseUri = "http://example.com";
        Parser parser = Parser.htmlParser();

        // When parsing the input string
        Document doc = parser.parseInput(html, baseUri);

        // Then the resulting document body should contain a <p> element with text "Test"
        Element p = doc.body().selectFirst("p");
        assertNotNull(p, "Expected a <p> element in the body");
        assertEquals("Test", p.text(), "The <p> element should contain the text 'Test'");
    }

    @Test
    @DisplayName("parseInput(Reader, String) with empty input returns an empty Document")
    public void test_TC02_O2() {
        // Given an empty reader, expecting branch through lock, parse call that yields empty body
        Reader reader = new StringReader("");
        String baseUri = "http://example.com";
        Parser parser = Parser.htmlParser();

        // When parsing the empty reader
        Document doc = parser.parseInput(reader, baseUri);

        // Then the document body should have zero child nodes
        assertEquals(0, doc.body().childNodeSize(), "Empty input should produce no nodes in body");
    }

    @Test
    @DisplayName("parseInput(Reader, String) when Reader throws IOException propagates UncheckedIOException")
    public void test_TC03_O2() {
        // Given a Reader stub that always throws IOException to drive the exception branch
        Reader failingReader = new Reader() {
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
        Parser parser = Parser.htmlParser();

        // When calling parseInput, then an UncheckedIOException should be thrown
        UncheckedIOException ex = assertThrows(UncheckedIOException.class, () -> {
            parser.parseInput(failingReader, baseUri);
        });
        // The cause of UncheckedIOException should be the original IOException with message "fail"
        assertNotNull(ex.getCause(), "UncheckedIOException should have a cause");
        assertEquals("fail", ex.getCause().getMessage(), "Cause message should match the underlying IOException");
    }
}