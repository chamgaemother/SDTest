package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseInput_0_Test {

    @Test
    @DisplayName("parseInput(String, String) delegates to Reader overload with non-empty HTML")
    public void test_TC01_O1() {
        // Using non-empty HTML to ensure branch B0->B1 (String overload path) is taken
        Parser parser = Parser.htmlParser();
        String html = "<p>Hello</p>";
        String base = "http://example.com";
        Document doc = parser.parseInput(html, base);
        // Expect that the parsed document text is Hello as the <p> tag content
        assertEquals("Hello", doc.text());
    }

    @Test
    @DisplayName("parseInput(Reader, String) returns Document for empty input")
    public void test_TC02_O2() {
        // Reader overload with empty input to cover path B2->B3 (empty case)
        Parser parser = Parser.htmlParser();
        Reader rdr = new StringReader("");
        String base = "base";
        Document doc = parser.parseInput(rdr, base);
        // Expect body text to be empty when input is empty
        assertEquals("", doc.body().text());
    }

    @Test
    @DisplayName("parseInput(String, String) throws NullPointerException when html is null")
    public void test_TC03_O1() {
        // Passing null html to trigger null check at B0 for String overload
        Parser parser = Parser.htmlParser();
        String html = null;
        String base = "u";
        assertThrows(NullPointerException.class, () -> parser.parseInput(html, base));
    }

    @Test
    @DisplayName("parseInput(Reader, String) propagates exception from TreeBuilder.parse")
    public void test_TC04_O2() {
        // StubTreeBuilder throws IllegalStateException to ensure propagation at path B2
        class StubTreeBuilder extends TreeBuilder {
            public StubTreeBuilder() {
                super(new Settings()); // Correct constructor call
            }

            @Override
            public void process(Token token) {
                // No implementation needed for this stub
            }

            @Override
            public Document parse(Reader in, String baseUri, Parser parser) {
                throw new IllegalStateException("fail");
            }
        }
        Parser parser = new Parser(new StubTreeBuilder());
        Reader rdr = new StringReader("x");
        String base = "u";
        // Expect that exception from TreeBuilder.parse is not caught internally
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> parser.parseInput(rdr, base));
        assertEquals("fail", ex.getMessage());
    }
}