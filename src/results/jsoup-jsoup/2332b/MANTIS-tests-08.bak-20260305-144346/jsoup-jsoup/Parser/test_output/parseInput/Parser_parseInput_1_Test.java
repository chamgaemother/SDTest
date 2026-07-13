package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.ParseErrorList;
import org.jsoup.parser.TreeBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseInput_1_Test {

    @Test
    @DisplayName("TC06: Reader overload throws IllegalArgumentException when baseUri is null and stubBuilder rejects it")
    public void test_TC06() {
        // GIVEN: A StringReader input and a stub TreeBuilder that throws IAE when baseUri is null (covers B_rd -> exception)
        Reader input = new StringReader("<div>ok</div>");
        TreeBuilder stubBuilder = new TreeBuilder() {
            @Override public ParseSettings defaultSettings() {
                // preserve case settings not relevant to exception path
                return new ParseSettings(ParseSettings.preserveCase);
            }
            @Override public TreeBuilder newInstance() { return this; }
            @Override public Document parse(Reader in, String baseUri, Parser parser) {
                // trigger the IAE when baseUri is null
                if (baseUri == null) throw new IllegalArgumentException("baseUri must not be null");
                return new Document("");
            }
            @Override public List<Node> parseFragment(Reader r, Element c, String b, Parser p) {
                return null;
            }
            @Override public boolean isContentForTagData(String n) { return false; }
            @Override public String defaultNamespace() { return ""; }
            @Override public void process(org.jsoup.parser.Token t) {
                // valid implementation to satisfy the abstract method
            }
        };
        Parser parser = new Parser(stubBuilder);
        String baseUri = null;
        // WHEN & THEN: parseInput(reader, null) should throw IllegalArgumentException as stubBuilder rejects null baseUri
        assertThrows(IllegalArgumentException.class, () -> parser.parseInput(input, baseUri),
            "Expected parseInput to throw IllegalArgumentException when baseUri is null");
    }

    @Test
    @DisplayName("TC07: String overload with real HtmlTreeBuilder returns a non-null Document containing parsed elements")
    public void test_TC07() {
        // GIVEN: A real HTML parser (HtmlTreeBuilder) and simple paragraph HTML (covers B_str -> B_rd -> B_exit)
        Parser parser = Parser.htmlParser();
        String html = "<p>Hello</p>";
        String baseUri = "http://example.com";
        // WHEN: parseInput(String, String) is invoked
        Document doc = parser.parseInput(html, baseUri);
        // THEN: Document should be non-null and contain one <p> in its body with text "Hello"
        assertNotNull(doc, "Parsed Document should not be null");
        List<Element> paragraphs = doc.body().select("p");
        assertEquals(1, paragraphs.size(), "Document body should contain exactly one <p> element");
        assertEquals("Hello", paragraphs.get(0).text(), "The <p> element text should be 'Hello'");
    }
}