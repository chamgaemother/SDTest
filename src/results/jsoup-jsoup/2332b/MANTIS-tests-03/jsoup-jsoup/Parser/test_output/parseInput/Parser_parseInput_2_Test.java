package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.Element;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_parseInput_2_Test {

    @Test
    @DisplayName("TC08: parseInput(Reader, String) wraps IOException from TreeBuilder.parse into RuntimeException")
    public void test_TC08() {
        // Design: stub TreeBuilder.parse to throw IOException to hit exception branch in parseInput(Reader, String)
        TreeBuilder throwingBuilder = new TreeBuilder() {
            @Override
            public Document parse(Reader input, String baseUri, Parser parser) throws IOException {
                throw new IOException("fail");
            }

            @Override
            public List<Node> parseFragment(Reader reader, Element context, String baseUri, Parser parser) {
                return null;
            }

            @Override
            public ParseSettings defaultSettings() {
                return new ParseSettings(ParseSettings.preserveCase); // using a valid constructor
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
            public void process(Token token) {} // Implementing the missing method

            @Override
            public String defaultNamespace() {
                return null;
            }
        };
        Parser parser = new Parser(throwingBuilder);
        Reader rdr = new StringReader("x");
        String base = "uri";
        // The call should wrap IOException into RuntimeException
        RuntimeException ex = assertThrows(RuntimeException.class, () -> parser.parseInput(rdr, base));
        assertNotNull(ex.getCause(), "Expected cause to be present");
        assertTrue(ex.getCause() instanceof IOException, "Cause should be IOException");
    }

    @Test
    @DisplayName("TC09: parseInput(String, String) wraps IOException from TreeBuilder.parse into RuntimeException")
    public void test_TC09() {
        // Design: stub TreeBuilder.parse to throw IOException to hit exception branch in parseInput(String, String) via Reader overload
        TreeBuilder throwingBuilder = new TreeBuilder() {
            @Override
            public Document parse(Reader input, String baseUri, Parser parser) throws IOException {
                throw new IOException("fail");
            }

            @Override
            public List<Node> parseFragment(Reader reader, Element context, String baseUri, Parser parser) {
                return null;
            }

            @Override
            public ParseSettings defaultSettings() {
                return new ParseSettings(ParseSettings.preserveCase); // using a valid constructor
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
            public void process(Token token) {} // Implementing the missing method

            @Override
            public String defaultNamespace() {
                return null;
            }
        };
        Parser parser = new Parser(throwingBuilder);
        String html = "<p>x</p>";
        String base = "uri";
        // Via parseInput(String, String), the StringReader path triggers the same exception wrapping
        RuntimeException ex = assertThrows(RuntimeException.class, () -> parser.parseInput(html, base));
        assertNotNull(ex.getCause(), "Expected cause to be present");
        assertTrue(ex.getCause() instanceof IOException, "Cause should be IOException");
    }
}