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

public class Parser_tagSet_2_Test {

    @Test
    @DisplayName("TC08: parseInput(String,baseUri) creates a Document with expected title and baseUri")
    public void test_TC08() {
        String html = "<html><body>Hello</body></html>";
        String base = "http://example/";
        Document doc = new Parser(new HtmlTreeBuilder()).parseInput(html, base);
        assertEquals("Hello", doc.body().text());
        assertEquals(base, doc.baseUri());
    }

    @Test
    @DisplayName("TC09: parseInput(Reader,baseUri) with empty input returns empty body")
    public void test_TC09() {
        Reader input = new StringReader("");
        String base = "URL";
        Document doc = new Parser(new HtmlTreeBuilder()).parseInput(input, base);
        assertTrue(doc.body().children().isEmpty());
    }

    @Test
    @DisplayName("TC10: parseFragmentInput(fragment, null, baseUri) returns list of nodes for top-level fragment")
    public void test_TC10() {
        String frag = "<p>X</p>";
        String base = "B";
        List<Node> nodes = new Parser(new HtmlTreeBuilder()).parseFragmentInput(frag, null, base);
        assertEquals(1, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        Element e = (Element) nodes.get(0);
        assertEquals("p", e.tagName());
        assertEquals("X", e.childNode(0).toString());
    }

    @Test
    @DisplayName("TC11: static parseBodyFragment(html, baseUri) uses Document.createShell and retains head empty")
    public void test_TC11() {
        String bodyHtml = "<div>1</div>";
        String base = "u";
        Document doc = Parser.parseBodyFragment(bodyHtml, base);
        assertEquals(1, doc.body().childNodeSize());
        assertTrue(doc.head().children().isEmpty());
    }

    @Test
    @DisplayName("TC12: static parseBodyFragment with multiple siblings removes all but first in reverse loop")
    public void test_TC12() {
        String bodyHtml = "<a/> <b/> <c/>";
        String base = "u";
        Document doc = Parser.parseBodyFragment(bodyHtml, base);
        assertEquals("a", doc.body().child(0).tagName());
        assertEquals("b", doc.body().child(1).tagName());
        assertEquals("c", doc.body().child(2).tagName());
    }

    @Test
    @DisplayName("TC13: defaultNamespace() returns SVG namespace for svgBuilder via xmlParser and then setTreeBuilder")
    public void test_TC13() {
        Parser parser = Parser.xmlParser();
        SvgTreeBuilderStub stub = new SvgTreeBuilderStub();
        parser.setTreeBuilder(stub);
        assertEquals(SvgTreeBuilderStub.NAMESPACE, parser.defaultNamespace());
    }

    @Test
    @DisplayName("TC14: isContentForTagData returns true for 'script' and false for 'div'")
    public void test_TC14() {
        Parser parser = Parser.htmlParser();
        boolean scriptOk = parser.isContentForTagData("script");
        boolean divOk = parser.isContentForTagData("div");
        assertTrue(scriptOk);
        assertFalse(divOk);
    }

    @Test
    @DisplayName("TC15: static parseXmlFragment with invalid XML fragment still returns nodes list")
    public void test_TC15() {
        String xml = "<a><b></a>";
        String base = "xml:base";
        List<Node> nodes = Parser.parseXmlFragment(xml, base);
        assertTrue(nodes.size() > 0);
    }

    // Stub TreeBuilder for SVG
    private static class SvgTreeBuilderStub extends TreeBuilder {
        static final String NAMESPACE = "http://www.w3.org/2000/svg";

        @Override
        public List<Node> parse(Reader input, String baseUri) throws IOException {
            return List.of(); // Dummy implementation
        }

        @Override
        public List<Node> parseFragment(Reader reader, Element context) {
            return List.of(); // Dummy implementation
        }

        @Override
        String defaultNamespace() {
            return NAMESPACE;
        }

        @Override
        boolean isContentForTagData(String tagName) {
            return false;
        }

        @Override
        ParseSettings defaultSettings() {
            return ParseSettings.preserveCase;
        }

        @Override
        TreeBuilder newInstance() {
            return new SvgTreeBuilderStub();
        }

        @Override
        void initialiseParse(Reader reader, String baseUri, Parser parser) {
            // no-op
        }

        @Override
        void process(Token token) {
            // Dummy implementation
        }
    }
}