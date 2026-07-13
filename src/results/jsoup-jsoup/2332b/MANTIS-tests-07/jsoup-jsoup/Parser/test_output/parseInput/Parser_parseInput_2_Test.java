package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.parser.TreeBuilder;
import org.jsoup.parser.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseInput_2_Test {

    /**
     * TEST TC11_O2_nullReturn parseInput Reader-null-return
     * GIVEN a TreeBuilder whose parse(...) returns null
     * WHEN parser.parseInput(reader, baseUri) is called
     * THEN the result should be null
     */
    @Test
    @DisplayName("parseInput(Reader, String) returns null when TreeBuilder.parse returns null")
    public void test_TC11_O2_nullReturn() {
        // Setup: a Reader that would normally supply some HTML
        Reader reader = new StringReader("<p>foo</p>");
        // Stub TreeBuilder: parse returns null to simulate null-return branch (B4)
        TreeBuilder stub = new TreeBuilder() {
            @Override
            public Document parse(Reader in, String uri, Parser parser) {
                // Directly return null to trigger the null-return path
                return null;
            }
            @Override
            public TreeBuilder newInstance() {
                // Return same stub for simplicity
                return this;
            }
            @Override
            public org.jsoup.parser.ParseSettings defaultSettings() {
                // Provide any non-null settings, using a valid constructor
                return new org.jsoup.parser.ParseSettings(org.jsoup.parser.ParseSettings.preserveCase);
            }
            @Override
            public void process(Token token) {
                // Implementing the abstract method from TreeBuilder
                // No operation needed for this test
            }
            @Override
            public java.util.List<org.jsoup.nodes.Node> parseFragment(Reader in, org.jsoup.nodes.Element context, String baseUri, Parser parser) {
                throw new UnsupportedOperationException("Not used in this test");
            }
        };
        Parser parser = new Parser(stub);

        // Invoke: should hit B0→B2→B3→B4 and return null
        Document result = parser.parseInput(reader, "http://base/");

        // Verify
        assertNull(result, "Expected parseInput to return null when TreeBuilder.parse returns null");
    }

    /**
     * TEST TC12_O2_uncheckedIO parseInput Reader throws UncheckedIOException
     * GIVEN a broken Reader that throws IOException on read(...)
     * AND a TreeBuilder.parse(...) that reads from the reader and wraps IOException in UncheckedIOException
     * WHEN parser.parseInput(brokenReader, baseUri) is called
     * THEN an UncheckedIOException is thrown with the original IOException as cause
     */
    @Test
    @DisplayName("parseInput(Reader, String) propagates UncheckedIOException when Reader.read fails")
    public void test_TC12_O2_uncheckedIO() {
        // Reader that throws IOException on read to force the exception path in TreeBuilder.parse (B3 ex→B5)
        Reader broken = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("read error");
            }
            @Override
            public void close() throws IOException {
                // no-op
            }
        };

        // Stub TreeBuilder: parse tries to read, catches IOException and throws UncheckedIOException
        TreeBuilder stub = new TreeBuilder() {
            @Override
            public Document parse(Reader in, String uri, Parser parser) {
                try {
                    // Attempt to read one char to trigger IOException
                    in.read(new char[1], 0, 1);
                } catch (IOException e) {
                    // Wrap and propagate as unchecked
                    throw new UncheckedIOException(e);
                }
                // Should not reach here
                return new Document(uri);
            }
            @Override
            public TreeBuilder newInstance() {
                return this;
            }
            @Override
            public org.jsoup.parser.ParseSettings defaultSettings() {
                return new org.jsoup.parser.ParseSettings(org.jsoup.parser.ParseSettings.preserveCase);
            }
            @Override
            public void process(Token token) {
                // Implementing the abstract method from TreeBuilder
                // No operation needed for this test
            }
            @Override
            public java.util.List<org.jsoup.nodes.Node> parseFragment(Reader in, org.jsoup.nodes.Element context, String baseUri, Parser parser) {
                throw new UnsupportedOperationException("Not used in this test");
            }
        };
        Parser parser = new Parser(stub);

        // Invoke & Verify: expect UncheckedIOException with cause message "read error"
        UncheckedIOException ex = assertThrows(UncheckedIOException.class,
            () -> parser.parseInput(broken, "http://u"),
            "Expected UncheckedIOException when reader.read fails");
        assertNotNull(ex.getCause(), "UncheckedIOException should have a cause");
        assertEquals("read error", ex.getCause().getMessage(),
            "The cause message should match the original IOException message");
    }
}