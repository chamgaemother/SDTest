package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.parser.ParseErrorList;
import org.jsoup.parser.Parser;
import org.jsoup.parser.XmlTreeBuilder;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_setTrackPosition_2_Test {

    @Test
    @DisplayName("TC10: parseInput(String,Reader) and parseInput(Reader,String) both produce equivalent Documents")
    public void test_TC10() {
        // Using simple HTML with title and body to cover both overloads
        String html = "<html><head><title>t</title></head><body><p>x</p></body></html>";
        Parser p1 = Parser.htmlParser();
        Parser p2 = Parser.htmlParser();
        Document d1 = p1.parseInput(html, "base"); // B2 path covers String overload
        Document d2 = p2.parseInput(new StringReader(html), "base"); // B5 path covers Reader overload
        assertEquals(d1.title(), d2.title(), "Titles should match for both overloads");
        assertEquals(d1.body().text(), d2.body().text(), "Body texts should match for both overloads");
    }

    @Test
    @DisplayName("TC11: parseFragmentInput on an Element context returns correct child nodes")
    public void test_TC11() {
        // Parse two span elements to ensure list size 2 and context remains unchanged
        Parser parser = Parser.htmlParser();
        Document doc = Document.createShell("uri");
        Element ctx = doc.body();
        String frag = "<span>a</span><span>b</span>";
        List<Node> nodes = parser.parseFragmentInput(frag, ctx, "uri"); // covers fragment parse
        assertEquals(2, nodes.size(), "Should return two nodes for two spans");
        assertTrue(ctx.children().isEmpty(), "Context element should not be modified");
    }

    @Test
    @DisplayName("TC12: static parseFragment overload with supplied ParseErrorList tracks errors")
    public void test_TC12() {
        // Provide mismatched tags to generate a parse error captured by supplied list
        String frag = "<div><span></div>"; // mismatched closing
        Element ctx = Document.createShell("u").body();
        ParseErrorList errs = ParseErrorList.tracking(1);
        List<Node> out = Parser.parseFragment(frag, ctx, "u", errs); // B9 with errorList
        assertEquals(1, errs.size(), "Error list should capture one mismatched-tag error up to limit");
        assertNotNull(out, "Output list should not be null even on errors");
    }

    @Test
    @DisplayName("TC13: parseXmlFragment returns XML nodes without HTML semantics")
    public void test_TC13() {
        // XML fragment should produce an element 'foo' without HTML namespace semantics
        String xml = "<foo>bar</foo>";
        List<Node> nodes = Parser.parseXmlFragment(xml, "base"); // XML parser path
        assertEquals(1, nodes.size(), "Should parse single XML element");
        Node node = nodes.get(0);
        assertTrue(node instanceof Element, "Parsed node should be an Element");
        Element el = (Element) node;
        assertEquals("foo", el.tagName(), "Tag name should be 'foo' for XML fragment");
    }

    @Test
    @DisplayName("TC14: parseBodyFragment populates body and preserves head shell")
    public void test_TC14() {
        // Body fragment should fill body only and leave head empty
        String bodyHtml = "<div>Hello</div>";
        Document doc = Parser.parseBodyFragment(bodyHtml, "u"); // B17 covers body fragment
        assertTrue(doc.head().children().isEmpty(), "Head should remain empty shell");
        Element div = doc.body().child(0);
        assertEquals("div", div.tagName(), "First body child should be <div>");
        assertEquals("Hello", doc.body().text(), "Body text should match 'Hello'");
    }

    @Test
    @DisplayName("TC15: setTreeBuilder swaps in XmlTreeBuilder and getTreeBuilder reflects change")
    public void test_TC15() {
        // Changing TreeBuilder from HTML to XML should be reflected and return same parser instance
        Parser parser = Parser.htmlParser();
        XmlTreeBuilder xtb = new XmlTreeBuilder();
        Parser ret = parser.setTreeBuilder(xtb); // setter chaining path
        assertSame(parser, ret, "setTreeBuilder should return same Parser instance");
        assertTrue(parser.getTreeBuilder() instanceof XmlTreeBuilder, "TreeBuilder should now be XmlTreeBuilder");
    }

    @Test
    @DisplayName("TC16: isContentForTagData distinguishes script/style vs normal tags")
    public void test_TC16() {
        // script should be data, div should not
        Parser p = Parser.htmlParser();
        boolean b1 = p.isContentForTagData("script"); // HTML raw text tags path
        boolean b2 = p.isContentForTagData("div"); // normal tags path
        assertTrue(b1, "Script content should be treated as Data Node");
        assertFalse(b2, "Div content should not be treated as Data Node");
    }

    @Test
    @DisplayName("TC17: defaultNamespace reflects HTML vs XML parser defaults")
    public void test_TC17() {
        // HTML parser default namespace differs from XML parser default namespace
        Parser ph = Parser.htmlParser();
        Parser px = Parser.xmlParser();
        String nh = ph.defaultNamespace();
        String nx = px.defaultNamespace();
        assertEquals(Parser.NamespaceHtml, nh, "HTML parser should use HTML namespace");
        assertEquals(Parser.NamespaceXml, nx, "XML parser should use XML namespace");
    }
}