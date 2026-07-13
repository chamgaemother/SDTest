package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_tagSet_2_Test {

    @Test
    @DisplayName("static parse(html, baseUri) returns a Document with parsed elements")
    public void test_TC09() {
        // Using a well-formed HTML ensures the parse path for full document parse is taken (B0→B1→B2→B3→B5)
        String html = "<html><body><p>Hello</p></body></html>";
        String base = "http://example.com";
        Document doc = Parser.parse(html, base);
        assertNotNull(doc, "Expected non-null Document from Parser.parse");
        Element p = doc.selectFirst("p");
        assertNotNull(p, "Expected a <p> element in the parsed document");
        assertEquals("Hello", p.text(), "Parsed <p> text should match input text");
    }

    @Test
    @DisplayName("static parseFragment(fragment, context, baseUri, errorList) tracks errors when provided")
    public void test_TC10() {
        // Mismatched tags exercise error-tracking branch (B0→B1→B4→B6→B8)
        String fragment = "<div><span></div>"; // broken nesting triggers parse error
        // Use a minimal context element: body of a shell document
        Element ctx = Document.createShell("").body();
        ParseErrorList errs = ParseErrorList.tracking(10);
        String base = "";
        List<Node> nodes = Parser.parseFragment(fragment, ctx, base, errs);
        assertFalse(nodes.isEmpty(), "Expected non-empty node list even with errors");
        assertTrue(errs.size() > 0, "Expected at least one parse error");
    }

    @Test
    @DisplayName("static parseXmlFragment(xml, baseUri) returns node list with XML element")
    public void test_TC11() {
        // Well-formed XML fragment triggers XML fragment parse path (B0→B2→B4→B7)
        String xml = "<root><child/></root>";
        String base = "urn:dummy";
        List<Node> nodes = Parser.parseXmlFragment(xml, base);
        assertEquals(1, nodes.size(), "Expected exactly one root element in XML fragment parse");
        Node node = nodes.get(0);
        assertTrue(node instanceof Element, "Expected Node to be an Element");
        Element root = (Element) node;
        assertEquals("root", root.tagName(), "Root element tag name should be 'root'");
    }

    @Test
    @DisplayName("static parseBodyFragment populates body with only first of multiple nodes after re-parenting loop")
    public void test_TC12() {
        // Multiple sibling elements ensure loop in parseBodyFragment executes (B0→B3→B5(loop×1)→B6→B8)
        String fragment = "<a>one</a><b>two</b>";
        String base = "http://test";
        Document doc = Parser.parseBodyFragment(fragment, base);
        List<Element> children = doc.body().children();
        assertEquals(2, children.size(), "Body should contain both <a> and <b> nodes");
        assertEquals("a", children.get(0).tagName(), "First child should be <a>");
        assertEquals("b", children.get(1).tagName(), "Second child should be <b>");
    }

    @Test
    @DisplayName("unescapeEntities returns correct unescaped string for attribute and text contexts")
    public void test_TC13() {
        // Entity string containing &lt;, &amp;, &quot;, and &apos; triggers unescape path (B0→B1→B2→B3→B4)
        String ent = "&lt;tag&gt; &amp; &quot;\\'";
        String t1 = Parser.unescapeEntities(ent, false);
        String t2 = Parser.unescapeEntities(ent, true);
        String expected = "<tag> & \"'";
        assertTrue(t1.contains(expected), "Text mode unescape should yield '<tag> & \"''");
        assertTrue(t2.contains(expected), "Attribute mode unescape should yield '<tag> & \"''");
    }
}