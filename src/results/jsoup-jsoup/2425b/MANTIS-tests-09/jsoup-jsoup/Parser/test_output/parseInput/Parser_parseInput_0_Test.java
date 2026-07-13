package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.parser.TreeBuilder;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.ParseErrorList;
import org.jsoup.parser.HtmlTreeBuilder;
import org.jsoup.parser.XmlTreeBuilder;

import java.io.Reader;
import java.io.StringReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseInput_0_Test {

    @Test
    @DisplayName("TC01: parseInput(String,baseUri) returns non-empty Document for simple HTML string")
    public void test_TC01() {
        String html = "<p>Hello</p>";
        String baseUri = "http://example.com";
        Parser parser = new Parser(new HtmlTreeBuilder());
        Document doc = parser.parseInput(html, baseUri);
        Element body = doc.body();
        assertEquals("Hello", body.select("p").text(), "Expected <p>Hello</p> to parse into a <p> with text 'Hello'");
    }

    @Test
    @DisplayName("TC02: parseInput(String,baseUri) returns empty Document for empty HTML string")
    public void test_TC02() {
        String html = "";
        String baseUri = "http://example.com";
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput(html, baseUri);
        assertEquals(0, doc.body().childNodeSize(), "Expected no nodes when parsing empty HTML");
    }

    @Test
    @DisplayName("TC03: parseInput(String,baseUri) throws NullPointerException when html string is null")
    public void test_TC03() {
        String html = null;
        String baseUri = "http://example.com";
        Parser parser = new Parser(new HtmlTreeBuilder());
        assertThrows(NullPointerException.class, () -> parser.parseInput(html, baseUri),
            "Expected NullPointerException when html is null");
    }

    @Test
    @DisplayName("TC04: parseInput(String,baseUri) accepts null baseUri and yields Document with null baseUri")
    public void test_TC04() {
        String html = "<div>Test</div>";
        String baseUri = null;
        Parser parser = new Parser(new HtmlTreeBuilder());
        Document doc = parser.parseInput(html, baseUri);
        assertNull(doc.baseUri(), "Expected baseUri() to be null when provided null");
    }

    @Test
    @DisplayName("TC05: parseInput(Reader,baseUri) returns Document for StringReader input")
    public void test_TC05() {
        Reader reader = new StringReader("<span>Foo</span>");
        String baseUri = "http://test";
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput(reader, baseUri);
        assertEquals("Foo", doc.body().select("span").text(), "Expected span text 'Foo' from reader input");
    }

    @Test
    @DisplayName("TC06: parseInput(Reader,baseUri) returns empty Document for Reader with no content")
    public void test_TC06() {
        Reader reader = new StringReader("");
        String baseUri = "";
        Parser parser = new Parser(new HtmlTreeBuilder());
        Document doc = parser.parseInput(reader, baseUri);
        assertEquals(0, doc.body().childNodeSize(), "Expected no child nodes for empty reader input");
    }

    @Test
    @DisplayName("TC07: parseInput(Reader,baseUri) throws UncheckedIOException when Reader.read throws IOException")
    public void test_TC07() {
        Reader reader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("fail");
            }
            @Override public void close() throws IOException { }
        };
        String baseUri = "u";
        Parser parser = new Parser(new HtmlTreeBuilder());
        assertThrows(UncheckedIOException.class, () -> parser.parseInput(reader, baseUri),
            "Expected UncheckedIOException when underlying reader throws IOException");
    }

    @Test
    @DisplayName("TC08: parseInput(String,baseUri) propagates RuntimeException thrown by TreeBuilder.parse")
    public void test_TC08() {
        // Implementing the required method process(Token token) in the anonymous TreeBuilder class
        TreeBuilder fake = new TreeBuilder() {
            @Override
            public Document parse(Reader in, String uri, Parser p) {
                process(null); // Call to process method
                throw new IllegalStateException("oops");
            }
            @Override
            public void process(Token token) {}
            @Override
            public Document parseFragment(Reader in, Element context, String uri) {
                return null; // Implementing as required
            }
            @Override
            public ParseSettings defaultSettings() { return ParseSettings.defaultSettings(); }
            @Override
            public TreeBuilder newInstance() { return this; }
            @Override
            public org.jsoup.nodes.TagSet defaultTagSet() { return null; }
            @Override
            public String defaultNamespace() { return ""; }
        };
        Parser parser = new Parser(fake);
        assertThrows(IllegalStateException.class, () -> parser.parseInput("<a/>", "u"),
            "Expected IllegalStateException from fake TreeBuilder.parse");
    }

    @Test
    @DisplayName("TC09: parseInput(String,baseUri) on XmlTreeBuilder returns Document with XML namespace preserved")
    public void test_TC09() {
        Parser parser = Parser.xmlParser();
        String xml = "<root xmlns='http://ns'></root>";
        String baseUri = "u";
        Document doc = parser.parseInput(xml, baseUri);
        Node root = doc.child(0);
        assertEquals(Parser.NamespaceXml, root.namespaceUri(),
            "Expected XML namespace to be Parser.NamespaceXml");
    }

    @Test
    @DisplayName("TC10: parseInput(Reader,baseUri) with long HTML containing multiple siblings returns all nodes")
    public void test_TC10() {
        String html = "<ul><li>1</li><li>2</li><li>3</li></ul>";
        Reader reader = new StringReader(html);
        String baseUri = "b";
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput(reader, baseUri);
        List<Element> items = doc.body().select("li");
        assertAll("Should have three <li> with texts 1,2,3 in order",
            () -> assertEquals(3, items.size(), "Expected exactly 3 list items"),
            () -> assertEquals("1", items.get(0).text(), "First item text"),
            () -> assertEquals("3", items.get(2).text(), "Third item text")
        );
    }
}