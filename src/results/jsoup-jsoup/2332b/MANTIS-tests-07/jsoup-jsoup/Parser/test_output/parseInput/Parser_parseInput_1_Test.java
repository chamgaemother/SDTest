package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseInput_1_Test {

    @Test
    @DisplayName("parseInput(String, String) returns null when TreeBuilder.parse returns null")
    public void test_TC11() {
        // Arrange: stub TreeBuilder to return null Document, fulfilling path B0→B1→B2→B3→B4
        class FakeTreeBuilder extends TreeBuilder {
            private final Document toReturn;
            FakeTreeBuilder(Document toReturn) { this.toReturn = toReturn; }
            @Override public ParseSettings defaultSettings() { return new ParseSettings(/* actual parameters */); }
            @Override public TreeBuilder newInstance() { return new FakeTreeBuilder(toReturn); }
            @Override public Document parse(Reader reader, String baseUri, Parser parser) {
                // Directly return null as per precondition
                return toReturn;
            }
            @Override public List<Node> parseFragment(Reader reader, @Nullable Element context, String baseUri, Parser parser) {
                return Collections.emptyList();
            }
            @Override public void initialiseParse(Reader reader, String baseUri, Parser parser) { /* no-op */ }
            @Override public String defaultNamespace() { return Parser.NamespaceHtml; }
            @Override public boolean isContentForTagData(String name) { return false; }
            @Override public void process(org.jsoup.parser.Token token) { /* no-op */ }
        }

        FakeTreeBuilder stub = new FakeTreeBuilder(null);
        Parser parser = new Parser(stub);
        String html = "<p>empty</p>";
        String baseUri = "http://example.com";

        // Act
        Document result = parser.parseInput(html, baseUri);

        // Assert: expect null as stubbed
        assertNull(result, "Expected parseInput to return null when TreeBuilder.parse returns null");
    }

    @Test
    @DisplayName("parseInput(Reader, String) propagates UncheckedIOException when Reader.read fails")
    public void test_TC12() {
        // Arrange: Reader that throws IOException on first read to drive exception path B0→B2→B3(ex)→B5
        Reader broken = new Reader() {
            @Override public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("read error");
            }
            @Override public void close() throws IOException { /* no-op */ }
        };
        // Stub TreeBuilder that attempts to read from the reader and wrap IOException
        class FakeTreeBuilder extends TreeBuilder {
            private final Document toReturn;
            FakeTreeBuilder(Document toReturn) { this.toReturn = toReturn; }
            @Override public ParseSettings defaultSettings() { return new ParseSettings(/* actual parameters */); }
            @Override public TreeBuilder newInstance() { return new FakeTreeBuilder(toReturn); }
            @Override public Document parse(Reader reader, String baseUri, Parser parser) {
                try {
                    // trigger the broken reader
                    reader.read(new char[10], 0, 10);
                } catch (IOException e) {
                    // intended behavior: wrap in UncheckedIOException
                    throw new UncheckedIOException(e);
                }
                return toReturn;
            }
            @Override public List<Node> parseFragment(Reader reader, @Nullable Element context, String baseUri, Parser parser) {
                return Collections.emptyList();
            }
            @Override public void initialiseParse(Reader reader, String baseUri, Parser parser) { /* no-op */ }
            @Override public String defaultNamespace() { return Parser.NamespaceHtml; }
            @Override public boolean isContentForTagData(String name) { return false; }
            @Override public void process(org.jsoup.parser.Token token) { /* no-op */ }
        }
        FakeTreeBuilder stub = new FakeTreeBuilder(new Document("u"));
        Parser parser = new Parser(stub);
        String baseUri = "http://u";

        // Act & Assert: UncheckedIOException should propagate
        UncheckedIOException ex = assertThrows(
            UncheckedIOException.class,
            () -> parser.parseInput(broken, baseUri),
            "Expected UncheckedIOException when Reader.read throws IOException"
        );
        // Also assert the cause message is preserved
        assertEquals("read error", ex.getCause().getMessage());
    }
}