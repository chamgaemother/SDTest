package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseInput_2_Test {

    @Test
    @DisplayName("parseInput(html, baseUri) returns a Document for valid HTML and baseUri (delegates to TreeBuilder.parse)")
    public void test_TC01() {
        // GIVEN a standard HTML parser and valid inputs, should take B0->B1->B2
        Parser parser = Parser.htmlParser();
        String html = "<span>jsoup</span>";
        String baseUri = "http://test";
        // WHEN
        Document doc = parser.parseInput(html, baseUri);
        // THEN: baseUri matches and parsed element exists
        assertEquals(baseUri, doc.baseUri(), "Document baseUri should match the input baseUri");
        Element span = doc.select("span").first();
        assertNotNull(span, "Parsed document should contain a <span> element");
        assertEquals("jsoup", span.text(), "Span text should be 'jsoup'");
    }

    @Test
    @DisplayName("parseInput(html, baseUri) throws NullPointerException when html is null")
    public void test_TC02() {
        // GIVEN null html should cause StringReader ctor NPE: B0->B1->B3
        Parser parser = Parser.htmlParser();
        String html = null;
        String baseUri = "http://test";
        // WHEN & THEN: expect NPE
        assertThrows(NullPointerException.class, () -> parser.parseInput(html, baseUri));
    }

    @Test
    @DisplayName("parseInput(html, baseUri) throws IllegalArgumentException when baseUri is null")
    public void test_TC03() {
        // GIVEN null baseUri, treeBuilder.parse should detect invalid URI: B0->B1->B3
        Parser parser = Parser.htmlParser();
        String html = "<p>x</p>";
        String baseUri = null;
        // WHEN & THEN: expect IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> parser.parseInput(html, baseUri));
    }

    @Test
    @DisplayName("parseInput(reader, baseUri) returns a Document when given a valid Reader and baseUri")
    public void test_TC04() {
        // GIVEN a standard HTML parser and valid reader, should take B0->B1->B2
        Parser parser = Parser.htmlParser();
        Reader reader = new StringReader("<ul><li>a</li><li>b</li></ul>");
        String baseUri = "http://site";
        // WHEN
        Document doc = parser.parseInput(reader, baseUri);
        // THEN: baseUri matches and two <li> elements parsed
        assertEquals(baseUri, doc.baseUri(), "Document baseUri should match input");
        List<Element> lis = doc.select("li");
        assertEquals(2, lis.size(), "There should be exactly 2 <li> elements");
        assertEquals("a", lis.get(0).text());
        assertEquals("b", lis.get(1).text());
    }

    @Test
    @DisplayName("parseInput(reader, baseUri) throws NullPointerException when reader is null")
    public void test_TC05() {
        // GIVEN null reader should trigger immediate NPE: B0->B1->B3
        Parser parser = Parser.htmlParser();
        Reader reader = null;
        String baseUri = "http://test";
        // WHEN & THEN: expect NPE
        assertThrows(NullPointerException.class, () -> parser.parseInput(reader, baseUri));
    }

    @Test
    @DisplayName("parseInput(reader, baseUri) propagates RuntimeException thrown by TreeBuilder.parse")
    public void test_TC06() {
        // GIVEN a stub TreeBuilder that throws RuntimeException(IOException), covering B0->B1->B4
        TreeBuilder stubBuilder = new TreeBuilder() {
            @Override
            public Document parse(Reader in, String baseUri, Parser p) {
                throw new RuntimeException(new IOException("err"));
            }
            @Override
            public void process(Token token) { }
            @Override
            public TreeBuilder newInstance() { return this; }
            @Override
            public ParseSettings defaultSettings() { return ParseSettings.defaultSettings(); } // Fixed to use defaultSettings()
        };
        Parser parser = new Parser(stubBuilder);
        Reader reader = new StringReader("x");
        String baseUri = "uri";
        // WHEN & THEN: RuntimeException with cause IOException
        RuntimeException ex = assertThrows(RuntimeException.class, () -> parser.parseInput(reader, baseUri));
        Throwable cause = ex.getCause();
        assertNotNull(cause, "Cause should not be null");
        assertTrue(cause instanceof IOException, "Cause should be an IOException");
        assertEquals("err", cause.getMessage(), "IOException message should be 'err'");
    }
}