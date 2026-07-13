package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Parser_parseInput_2_Test {

    @Test
    @DisplayName("parseInput(Reader, baseUri) propagates RuntimeException from custom TreeBuilder.parse")
    public void test_TC07() {
        // Setup a TreeBuilder stub whose parse(...) throws IllegalStateException to follow path B0→B2→B3→B5
        TreeBuilder stubBuilder = new TreeBuilder() {
            @Override
            public TreeBuilder newInstance() {
                // newInstance called in copy constructor, but here parser.parseInput uses original builder
                return this;
            }

            @Override
            public ParseSettings defaultSettings() {
                // defaultSettings called by Parser constructor
                return new ParseSettings(true, true); // Adjusted to use existing constructor
            }

            @Override
            public Document parse(Reader in, String baseUri, Parser parser) {
                // throw runtime exception to simulate failure in parse
                throw new IllegalStateException("boom");
            }

            @Override
            public List<Node> parseFragment(Reader in, Element context, String baseUri, Parser parser) {
                // not used in this scenario
                return null;
            }

            @Override
            public void process(org.jsoup.parser.Token token) {
                // Implementing the missing abstract method
            }
        };
        Parser parser = new Parser(stubBuilder);
        Reader reader = new StringReader("<p>irrelevant</p>");
        // Expect IllegalStateException propagated from stubBuilder.parse
        assertThrows(IllegalStateException.class, () -> parser.parseInput(reader, "http://example.com"));
    }

    @Test
    @DisplayName("parseInput(String, baseUri) with null baseUri throws NullPointerException in TreeBuilder.parse")
    public void test_TC08() {
        // Use default HtmlTreeBuilder so parseInput(String,String) → parseInput(Reader,String) → parse throws NPE when baseUri is null
        Parser parser = new Parser(new HtmlTreeBuilder());
        String html = "<p>X</p>";
        String baseUri = null;
        // Passing null baseUri should cause a NullPointerException in downstream parse
        assertThrows(NullPointerException.class, () -> parser.parseInput(html, baseUri));
    }
}