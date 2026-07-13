package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Token;
import org.jsoup.parser.TreeBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseInput_0_Test {

    static class FakeTreeBuilderReturningDoc extends TreeBuilder {
        private final String stubTitle;

        FakeTreeBuilderReturningDoc(String stubTitle) {
            this.stubTitle = stubTitle;
        }

        @Override
        public ParseSettings defaultSettings() {
            return new HtmlTreeBuilder().defaultSettings();
        }

        @Override
        public Document parse(Reader input, String baseUri, Parser parser) {
            return new Document(baseUri) {
                @Override
                public String title() {
                    return stubTitle;
                }
            };
        }

        @Override
        public List<Node> parseFragment(Reader input, Element context, String baseUri, Parser parser) {
            throw new UnsupportedOperationException("Not used in parseInput tests");
        }

        @Override
        public void process(Token token) {} // Implemented to satisfy TreeBuilder contract
    }

    static class FakeTreeBuilderThrowingNPE extends TreeBuilder {
        @Override
        public ParseSettings defaultSettings() {
            return new HtmlTreeBuilder().defaultSettings();
        }

        @Override
        public Document parse(Reader input, String baseUri, Parser parser) {
            throw new NullPointerException("reader was null");
        }

        @Override
        public List<Node> parseFragment(Reader input, Element context, String baseUri, Parser parser) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void process(Token token) {} // Implemented to satisfy TreeBuilder contract
    }

    static class FakeTreeBuilderThrowingIllegalState extends TreeBuilder {
        private final IllegalStateException exToThrow;

        FakeTreeBuilderThrowingIllegalState(IllegalStateException ex) {
            this.exToThrow = ex;
        }

        @Override
        public ParseSettings defaultSettings() {
            return new HtmlTreeBuilder().defaultSettings();
        }

        @Override
        public Document parse(Reader input, String baseUri, Parser parser) {
            throw exToThrow;
        }

        @Override
        public List<Node> parseFragment(Reader input, Element context, String baseUri, Parser parser) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void process(Token token) {} // Implemented to satisfy TreeBuilder contract
    }

    @Test
    @DisplayName("TC01_O1: String overload with valid html and baseUri invokes Reader overload and returns Document")
    void test_TC01_O1() {
        Parser p = new Parser(new FakeTreeBuilderReturningDoc("ok"));
        String html = "<p>test</p>";
        String baseUri = "http://example.com";

        Document result = p.parseInput(html, baseUri);

        assertEquals("ok", result.title(), "Expected the stubbed title \"ok\" from FakeTreeBuilderReturningDoc");
    }

    @Test
    @DisplayName("TC02_O1: String overload with empty html returns Document from Reader overload")
    void test_TC02_O1() {
        Parser p = new Parser(new FakeTreeBuilderReturningDoc("empty"));
        String html = "";
        String baseUri = "http://example.com";

        Document result = p.parseInput(html, baseUri);

        assertEquals("empty", result.title(), "Expected the stubbed title \"empty\" for empty input");
    }

    @Test
    @DisplayName("TC03_O1: String overload with null html throws NullPointerException at StringReader ctor")
    void test_TC03_O1() {
        Parser p = Parser.htmlParser();
        String html = null;
        String baseUri = "http://example.com";

        assertThrows(NullPointerException.class, () -> p.parseInput(html, baseUri),
            "Expected NullPointerException when html is null");
    }

    @Test
    @DisplayName("TC04_O2: Reader overload with valid Reader and baseUri returns Document")
    void test_TC04_O2() {
        Reader in = new StringReader("<div>ok</div>");
        Parser p = new Parser(new FakeTreeBuilderReturningDoc("fromReader"));
        String baseUri = "http://x";

        Document result = p.parseInput(in, baseUri);

        assertEquals("fromReader", result.title(), "Expected stubbed title \"fromReader\"");
    }

    @Test
    @DisplayName("TC05_O2: Reader overload with null Reader throws NullPointerException from treeBuilder.parse")
    void test_TC05_O2() {
        Parser p = new Parser(new FakeTreeBuilderThrowingNPE());
        Reader in = null;
        String baseUri = "uri";

        assertThrows(NullPointerException.class, () -> p.parseInput(in, baseUri),
            "Expected NullPointerException when reader is null");
    }

    @Test
    @DisplayName("TC06_O2: Reader overload with null baseUri returns Document if TreeBuilder handles null baseUri")
    void test_TC06_O2() {
        Reader in = new StringReader("<i>x</i>");
        Parser p = new Parser(new FakeTreeBuilderReturningDoc("nullBase"));
        String baseUri = null;

        Document result = p.parseInput(in, baseUri);

        assertEquals("nullBase", result.title(), "Expected stubbed title \"nullBase\" even when baseUri is null");
    }

    @Test
    @DisplayName("TC07_O2: Reader overload when TreeBuilder.parse throws custom ParseException propagates exception")
    void test_TC07_O2() {
        Reader in = new StringReader("<b>bad</b>");
        Parser p = new Parser(new FakeTreeBuilderThrowingIllegalState(new IllegalStateException("parse error")));
        String baseUri = "u";

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> p.parseInput(in, baseUri),
            "Expected IllegalStateException propagated from builder.parse");
        assertEquals("parse error", ex.getMessage(), "Expected message \"parse error\"");
    }
}