package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseInput_0_Test {

    @Test
    @DisplayName("parseInput(String, String) throws NullPointerException when html is null")
    public void test_TC01_O1() {
        // html is null triggers NPE before delegation
        String html = null;
        String baseUri = "http://example.com";
        assertThrows(NullPointerException.class, () -> {
            Parser.htmlParser().parseInput(html, baseUri);
        });
    }

    @Test
    @DisplayName("parseInput(String, String) throws NullPointerException when baseUri is null")
    public void test_TC02_O1() {
        // baseUri null should cause NPE in parseInput overload
        String html = "<p>test</p>";
        String baseUri = null;
        assertThrows(NullPointerException.class, () -> {
            Parser.htmlParser().parseInput(html, baseUri);
        });
    }

    @Test
    @DisplayName("parseInput(Reader, String) throws NullPointerException when reader is null")
    public void test_TC03_O2() {
        // reader null branch triggers NPE in overload
        Reader reader = null;
        String baseUri = "http://example.com";
        assertThrows(NullPointerException.class, () -> {
            Parser.htmlParser().parseInput(reader, baseUri);
        });
    }

    @Test
    @DisplayName("parseInput(Reader, String) throws NullPointerException when baseUri is null")
    public void test_TC04_O2() {
        // baseUri null for Reader overload triggers NPE
        Reader reader = new StringReader("<p>x</p>");
        String baseUri = null;
        assertThrows(NullPointerException.class, () -> {
            Parser.htmlParser().parseInput(reader, baseUri);
        });
    }

    @Test
    @DisplayName("parseInput(String, String) delegates to treeBuilder.parse and returns its Document instance")
    public void test_TC05_O1() {
        // Use stub builder that returns fixed Document to test delegation branch
        Document stubDoc = new Document("stub");
        DummyTreeBuilder stub = new DummyTreeBuilder(stubDoc);
        Parser parser = new Parser(stub);
        String html = "<tag/>";
        String baseUri = "base";
        Document result = parser.parseInput(html, baseUri);
        assertSame(stubDoc, result, "Expected parseInput to return the stub document instance");
    }

    @Test
    @DisplayName("parseInput(Reader, String) delegates to treeBuilder.parse and returns its Document instance")
    public void test_TC06_O2() {
        // Use stub builder for Reader overload delegation
        Document stubDoc = new Document("stubReader");
        DummyTreeBuilder stub = new DummyTreeBuilder(stubDoc);
        Parser parser = new Parser(stub);
        Reader reader = new StringReader("<t></t>");
        String baseUri = "base";
        Document result = parser.parseInput(reader, baseUri);
        assertSame(stubDoc, result, "Expected parseInput to return the stub document instance from Reader overload");
    }

    @Test
    @DisplayName("parseInput(Reader, String) with HtmlTreeBuilder parses simple HTML fragment into Document body text")
    public void test_TC07_O2() {
        // Real HtmlTreeBuilder integration: simple <p>hello</p> should appear in body text
        Reader reader = new StringReader("<p>hello</p>");
        String baseUri = "http://u";
        Document result = Parser.htmlParser().parseInput(reader, baseUri);
        assertAll(
            () -> assertEquals("hello", result.body().text(), "Body text should be 'hello'"),
            () -> assertEquals(baseUri, result.baseUri(), "Base URI should be preserved")
        );
    }

    /**
     * Dummy TreeBuilder stub that returns a fixed Document instance for delegation tests.
     */
    private static class DummyTreeBuilder extends TreeBuilder {
        private final Document docStub;
        DummyTreeBuilder(Document docStub) {
            this.docStub = docStub;
        }
        @Override
        public Document parse(Reader input, String baseUri, Parser parser) {
            return docStub;
        }
        @Override
        public List<Node> parseFragment(Reader reader, Element context, String baseUri, Parser parser) {
            throw new UnsupportedOperationException();
        }
        @Override
        public TreeBuilder newInstance() {
            return new DummyTreeBuilder(docStub);
        }
        @Override
        public ParseSettings defaultSettings() {
            // Delegate to HTML default
            return new HtmlTreeBuilder().defaultSettings();
        }
        @Override
        public void process(org.jsoup.parser.Token token) {
            // Implementing the required abstract method from TreeBuilder
            // No operation needed for this test
        }
    }
}