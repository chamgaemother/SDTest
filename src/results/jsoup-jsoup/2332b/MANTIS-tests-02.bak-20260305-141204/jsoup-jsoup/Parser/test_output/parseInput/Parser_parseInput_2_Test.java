package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.parser.HtmlTreeBuilder;
import org.jsoup.parser.XmlTreeBuilder;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.ParseErrorList;
import org.jsoup.parser.TreeBuilder;
import org.jsoup.parser.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseInput_2_Test {

    @Test
    @DisplayName("parseInput(Reader, String) with null baseUri in reader overload throws NullPointerException")
    public void test_TC11() {
        // Create stub TreeBuilder that throws NPE when baseUri is null in parse
        TreeBuilder stubBuilder = new TreeBuilder() {
            @Override
            public Document parse(Reader input, String baseUri, Parser parser) {
                if (baseUri == null) {
                    throw new NullPointerException("baseUri must not be null");
                }
                return Document.createShell(baseUri);
            }

            @Override
            public List<Node> parseFragment(Reader input, Element context, String baseUri, Parser parser) {
                return null;
            }

            @Override
            public ParseSettings defaultSettings() {
                return null;
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
                return null;
            }

            @Override
            public void process(Token token) {
                // No processing needed for this test
            }
        };
        Parser parser = new Parser(stubBuilder);
        Reader rdr = new StringReader("<p>test</p>");
        String baseUri = null;
        // Expect NPE when baseUri is null to satisfy path B0→B3→B5→exception
        assertThrows(NullPointerException.class, () -> parser.parseInput(rdr, baseUri));
    }

    @Test
    @DisplayName("parseInput(Reader, String) with real HtmlTreeBuilder returns a Document with expected element")
    public void test_TC12() {
        // Use real HTML parser to traverse normal path B0→B3→B4→B5
        Parser parser = Parser.htmlParser();
        Reader rdr = new StringReader("<span id='foo'>bar</span>");
        String baseUri = "http://example.com";
        Document doc = parser.parseInput(rdr, baseUri);
        // Verify that a <span id='foo'> is present in body
        Element span = doc.body().select("span#foo").first();
        assertNotNull(span, "Expected span#foo element to be parsed into the document body");
    }

    @Test
    @DisplayName("parseInput(String, String) with xmlParser returns Document with root element")
    public void test_TC13() {
        // Use XML parser on string overload to cover B0→B1→B4→B5
        Parser parser = Parser.xmlParser();
        String xml = "<root><leaf/></root>";
        String baseUri = "";
        Document doc = parser.parseInput(xml, baseUri);
        // The first child node of the document should be the root element
        assertEquals("root", doc.child(0).nodeName(), "Expected root element nodeName to be 'root'");
    }

    @Test
    @DisplayName("parseInput(String, String) when TreeBuilder.parse returns null propagates null return")
    public void test_TC14() {
        // Create stub TreeBuilder that returns null in parse to test null propagation (B0→B1→B4→B5)
        TreeBuilder stubBuilder = new TreeBuilder() {
            @Override
            public Document parse(Reader input, String baseUri, Parser parser) {
                return null;
            }

            @Override
            public List<Node> parseFragment(Reader input, Element context, String baseUri, Parser parser) {
                return null;
            }

            @Override
            public ParseSettings defaultSettings() {
                return null;
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
                return null;
            }

            @Override
            public void process(Token token) {
                // No processing needed for this test
            }
        };
        Parser parser = new Parser(stubBuilder);
        String html = "<ignored/>";
        String baseUri = "http://x";
        // When stub parse returns null, parseInput should propagate the null result
        Document result = parser.parseInput(html, baseUri);
        assertNull(result, "Expected parseInput to return null when underlying parse returns null");
    }
}