package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
public class Parser_parseInput_1_Test {

    @Test
    @DisplayName("parseInput(Reader,baseUri) propagates RuntimeException thrown by TreeBuilder.parse")
    public void test_TC11() {
        // Arrange a stub TreeBuilder that always throws IllegalStateException on parse
        TreeBuilder stub = new TreeBuilder() {
            @Override
            public Document parse(Reader in, String uri, Parser p) {
                throw new IllegalStateException("oops");
            }

            @Override
            public void process(Token token) {
                // Implementing the abstract method to avoid compilation error
            }

            @Override
            public Document parseFragment(Reader in, Element context, String uri) {
                return null; // Changed method signature to match TreeBuilder
            }

            @Override
            public ParseSettings defaultSettings() {
                return ParseSettings.htmlDefault();
            }

            @Override
            public TreeBuilder newInstance() {
                return this;
            }

            @Override
            public org.jsoup.nodes.TagSet defaultTagSet() {
                return org.jsoup.nodes.TagSet.htmlDefault();
            }

            @Override
            public String defaultNamespace() {
                return "";
            }
        };
        Parser parser = new Parser(stub);
        Reader reader = new StringReader("<a/>);
        String baseUri = "u";
        // When & Then: calling parseInput should propagate the IllegalStateException thrown by the stub
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
            () -> parser.parseInput(reader, baseUri));
        assertEquals("oops", thrown.getMessage());
    }

    @Test
    @DisplayName("parseInput(String,baseUri) on XmlTreeBuilder returns Document preserving XML namespace")
    public void test_TC12() {
        // Using the XML parser to ensure XML namespace path is taken and default namespace is applied
        String xml = "<root xmlns='http://ns'></root>";
        String baseUri = "http://x";
        Parser parser = Parser.xmlParser();
        // Parse the input string; this exercises parseInput(String, baseUri) which delegates to the XmlTreeBuilder
        Document doc = parser.parseInput(xml, baseUri);
        // The document's first child should be the <root> element with the XML namespace URI set to Parser.NamespaceXml
        Node firstChild = doc.childNode(0);
        if (!(firstChild instanceof Element)) {
            throw new AssertionError("Expected first child to be an Element");
        }
        Element root = (Element) firstChild;
        assertEquals(Parser.NamespaceXml, root.namespaceUri()); // Fixed namespace reference
    }
}