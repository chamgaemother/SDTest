package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.parser.TreeBuilder;
import org.jsoup.parser.ParseSettings;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseInput_1_Test {

    @Test
    @DisplayName("parseInput(Reader,baseUri) propagates unexpected RuntimeException from TreeBuilder.parse")
    public void test_TC06() {
        // Arrange: stub TreeBuilder throws IllegalStateException to drive exception path
        TreeBuilder stub = new TreeBuilder() {
            @Override public Document parse(Reader in, String uri, Parser p) {
                throw new IllegalStateException("bad");
            }
            @Override public List<Node> parseFragment(Reader in, Element ctx, String uri, Parser p) { return null; }
            @Override public TreeBuilder newInstance() { return this; }
            @Override public ParseSettings defaultSettings() { return ParseSettings.htmlDefault; }
            @Override public org.jsoup.parser.TagSet defaultTagSet() { return org.jsoup.parser.TagSet.html(); }
            @Override public String defaultNamespace() { return ""; }
            @Override public void process(org.jsoup.parser.Token tok) { /* implementation here */ }
        };
        Parser parser = new Parser(stub);
        Reader reader = new StringReader("<tag/>"); // Fixed: Added missing closing quote
        // Act & Assert: expect the same IllegalStateException with message "bad"
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> parser.parseInput(reader, "uri"));
        assertEquals("bad", ex.getMessage());
    }

    @Test
    @DisplayName("parseInput(String,baseUri) throws NullPointerException when html string is null")
    public void test_TC07() {
        Parser parser = Parser.htmlParser();
        String html = null;
        String baseUri = "http://x";
        // Act & Assert: passing null html should trigger NullPointerException in overload
        assertThrows(NullPointerException.class, () -> parser.parseInput(html, baseUri));
    }

    @Test
    @DisplayName("parseInput(Reader,baseUri) accepts null baseUri and passes it through to TreeBuilder.parse")
    public void test_TC08() {
        // Arrange: stub TreeBuilder records passed baseUri (which is null) and returns dummy Document
        AtomicReference<String> rec = new AtomicReference<>();
        Document DUMMY = new Document("stub://");
        TreeBuilder stub = new TreeBuilder() {
            @Override public Document parse(Reader in, String uri, Parser p) {
                rec.set(uri);
                return DUMMY;
            }
            @Override public List<Node> parseFragment(Reader in, Element ctx, String uri, Parser p) { return null; }
            @Override public TreeBuilder newInstance() { return this; }
            @Override public ParseSettings defaultSettings() { return ParseSettings.htmlDefault; }
            @Override public org.jsoup.parser.TagSet defaultTagSet() { return org.jsoup.parser.TagSet.html(); }
            @Override public String defaultNamespace() { return ""; }
            @Override public void process(org.jsoup.parser.Token tok) { /* implementation here */ }
        };
        Parser parser = new Parser(stub);
        Reader reader = new StringReader("<x/>");
        String baseUri = null;
        // Act: call parseInput with null baseUri exercises branch where uri passed unchanged
        Document result = parser.parseInput(reader, baseUri);
        // Assert: the returned Document is exactly DUMMY, and recorded baseUri is null
        assertSame(DUMMY, result);
        assertNull(rec.get());
    }
}