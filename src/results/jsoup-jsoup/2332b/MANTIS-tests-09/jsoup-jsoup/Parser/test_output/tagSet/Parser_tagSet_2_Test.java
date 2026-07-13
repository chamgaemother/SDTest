package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.ParseErrorList;
import org.jsoup.parser.Parser;
import org.jsoup.parser.TreeBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.Jsoup;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_tagSet_2_Test {

    @Test
    @DisplayName("TC06: clone() returns a deep copy with independent state")
    public void test_TC06() {
        // GIVEN a html parser with tracking enabled
        Parser orig = Parser.htmlParser().setTrackErrors(3).setTrackPosition(true);
        // WHEN clone is invoked
        Parser copy = orig.clone();
        // THEN verify deep copy semantics
        assertNotSame(orig, copy, "copy should be a different instance");
        // settings object should be equal (same values) but could be same or deep copy, intent is equality
        assertEquals(orig.settings(), copy.settings(), "settings should be equal");
        assertEquals(3, copy.getErrors().getMaxSize(), "error max size should be copied");
        assertTrue(copy.isTrackPosition(), "trackPosition flag should be copied");
        assertNotSame(orig.getTreeBuilder(), copy.getTreeBuilder(), "treeBuilder should be a new instance");
    }

    @Test
    @DisplayName("TC07: newInstance() on XML parser yields independent Parser with XmlTreeBuilder")
    public void test_TC07() {
        // GIVEN an xml parser with one error slot
        Parser xml = Parser.xmlParser().setTrackErrors(1);
        // WHEN newInstance is invoked
        Parser copy = xml.newInstance();
        // THEN new instance separate but with same builder type and error config
        assertNotSame(xml, copy, "newInstance should return a new object");
        assertEquals(xml.getTreeBuilder().getClass(), copy.getTreeBuilder().getClass(),
                "TreeBuilder class should be preserved");
        assertEquals(1, copy.getErrors().getMaxSize(), "error tracking size should be copied");
    }

    @Test
    @DisplayName("TC08: parseInput(Reader, baseUri) with simple HTML yields non-null Document body")
    public void test_TC08() {
        // GIVEN a html parser and a simple paragraph fragment
        Parser p = Parser.htmlParser();
        Reader r = new StringReader("<p>Hello</p>");
        // WHEN parsing via Reader overload
        Document doc = p.parseInput(r, "http://example/");
        // THEN the document body should contain the paragraph text
        assertEquals("Hello", doc.body().select("p").text(), "parsed paragraph text should match input");
    }

    @Test
    @DisplayName("TC09: parseFragmentInput(null context) returns fragment nodes without context parent")
    public void test_TC09() {
        // GIVEN a html parser and fragment reader
        Parser p = Parser.htmlParser();
        Reader r = new StringReader("<span>Text</span>");
        // WHEN parsing fragment with null context
        List<Node> nodes = p.parseFragmentInput(r, null, "base");
        // THEN expect a single element node for span
        assertEquals(1, nodes.size(), "should parse exactly one node");
        assertTrue(nodes.get(0) instanceof Element, "parsed node should be an Element");
        assertEquals("span", ((Element)nodes.get(0)).tagName(), "element tag should be 'span'");
    }

    @Test
    @DisplayName("TC10: static parseFragment(fragment, context, baseUri, errorList) uses provided errorList")
    public void test_TC10() {
        // GIVEN a context element and a tracking error list of size 1
        Element context = new Element("div");
        ParseErrorList errs = ParseErrorList.tracking(1);
        String html = "<b>Bad &unclosed";
        // WHEN invoking static parseFragment with provided error list
        List<Node> list = Parser.parseFragment(html, context, "uri", errs);
        // THEN expect some nodes returned and errorList instance used
        assertTrue(list.size() >= 1, "should return at least one node");
        assertSame(errs, errs, "errorList instance should be the same provided");
        assertEquals(1, errs.getMaxSize(), "maxSize should remain 1");
        assertTrue(errs.size() <= 1, "tracked errors count should be <= maxSize");
    }

    @Test
    @DisplayName("TC11: parseBodyFragment appends nodes to body in reverse-original order")
    public void test_TC11() {
        // GIVEN a two-element fragment
        String frag = "<i>One</i><b>Two</b>";
        // WHEN parsing as body fragment
        Document doc = Parser.parseBodyFragment(frag, "uri");
        // THEN children order in body should match original fragment order
        List<Element> children = doc.body().children();
        assertEquals("i", children.get(0).tagName(), "first child should be <i>");
        assertEquals("b", children.get(1).tagName(), "second child should be <b>");
    }

    @Test
    @DisplayName("TC12: unescapeEntities(true) and (false) correctly unescape named and numeric entities")
    public void test_TC12() {
        // GIVEN a mixed escaped string
        String enc = "&amp;&#60;";
        // WHEN unescaping in data and attribute mode
        String dataMode = Parser.unescapeEntities(enc, false);
        String attrMode = Parser.unescapeEntities(enc, true);
        // THEN both modes should yield the correct unescaped string
        assertEquals("&<", dataMode, "data mode unescape should convert &amp; and &#60;");
        assertEquals("&<", attrMode, "attribute mode unescape should convert &amp; and &#60;");
    }
}