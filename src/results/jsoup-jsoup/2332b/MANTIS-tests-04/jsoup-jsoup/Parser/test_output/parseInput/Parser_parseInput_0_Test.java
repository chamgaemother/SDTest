package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.parser.TreeBuilder;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.ParseErrorList;
import org.jsoup.parser.Token;
import org.jsoup.parser.HtmlTreeBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseInput_0_Test {

    @Test
    @DisplayName("parseInput(String, String) with valid HTML delegates to treeBuilder.parse and returns a non-null Document (baseUri applied)")
    public void test_TC01_O1() {
        String html = "<p>hello</p>";
        String baseUri = "http://example.com/";
        Parser parser = new Parser(new HtmlTreeBuilder());
        Document doc = parser.parseInput(html, baseUri);
        assertNotNull(doc, "Expected non-null Document");
        assertEquals(baseUri, doc.baseUri(), "Expected baseUri to be applied");
        assertEquals("hello", doc.body().select("p").text(), "Expected <p> element with text 'hello'");
    }

    @Test
    @DisplayName("parseInput(String, String) with empty HTML returns a Document with empty body")
    public void test_TC02_O1() {
        String html = "";
        String baseUri = "http://example.com/";
        Parser parser = new Parser(new HtmlTreeBuilder());
        Document doc = parser.parseInput(html, baseUri);
        assertTrue(doc.body().children().isEmpty(), "Expected body to have no children for empty input");
    }

    @Test
    @DisplayName("parseInput(String, String) with null html throws NullPointerException when creating StringReader")
    public void test_TC03_O1() {
        String html = null;
        String baseUri = "http://example.com/";
        Parser parser = new Parser(new HtmlTreeBuilder());
        assertThrows(NullPointerException.class,
            () -> parser.parseInput(html, baseUri),
            "Expected NullPointerException when html is null");
    }

    @Test
    @DisplayName("parseInput(Reader, String) with stub TreeBuilder that throws at parse invokes exception path")
    public void test_TC04_O2() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        TreeBuilder stub = new TreeBuilder() {
            @Override
            public Document parse(Reader input, String baseUri, Parser parser) {
                throw new IllegalStateException("fail");
            }
            @Override
            public List<Node> parseFragment(Reader reader, Element context, String baseUri, Parser parser) {
                return null;
            }
            @Override
            public ParseSettings defaultSettings() {
                return ParseSettings.preserveCase;
            }
            @Override
            public TreeBuilder newInstance() {
                return this;
            }
            @Override
            public boolean isContentForTagData(String normalName) {
                return false;
            }
            @Override
            public String defaultNamespace() {
                return "";
            }
            @Override
            public void initialiseParse(Reader input, String baseUri, Parser parser) {
                // no-op
            }
            @Override
            public void process(Token token) { /* no-op */ }
        };
        parser.setTreeBuilder(stub);
        Reader reader = new StringReader("x");
        String baseUri = "http://a/";
        assertThrows(IllegalStateException.class,
            () -> parser.parseInput(reader, baseUri),
            "Expected IllegalStateException from stub TreeBuilder");
    }

    @Test
    @DisplayName("parseInput(Reader, String) with valid Reader returns Document matching input")
    public void test_TC05_O2() {
        Reader reader = new StringReader("<div>test</div>");
        String baseUri = "http://example.org/";
        Parser parser = new Parser(new HtmlTreeBuilder());
        Document doc = parser.parseInput(reader, baseUri);
        assertEquals("test", doc.body().select("div").text(), "Expected <div> element with text 'test'");
    }
}