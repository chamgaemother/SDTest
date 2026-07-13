package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.ParseErrorList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_tagSet_1_Test {

    @Test
    @DisplayName("TC01: setTrackErrors enables and disables error tracking and affects isTrackErrors")
    public void test_TC01() {
        // GIVEN a fresh HTML parser
        Parser parser = Parser.htmlParser();
        // WHEN enabling error tracking with a positive max
        parser.setTrackErrors(5);
        // THEN isTrackErrors should reflect enabled tracking (maxSize > 0)
        assertTrue(parser.isTrackErrors(), "Expected tracking enabled when maxErrors > 0");
        // WHEN disabling error tracking by setting max to zero
        parser.setTrackErrors(0);
        // THEN isTrackErrors should reflect disabled tracking (maxSize == 0)
        assertFalse(parser.isTrackErrors(), "Expected tracking disabled when maxErrors == 0");
    }

    @Test
    @DisplayName("TC02: setTrackPosition toggles position tracking and isTrackPosition reflects it")
    public void test_TC02() {
        // GIVEN a fresh XML parser
        Parser parser = Parser.xmlParser();
        // WHEN enabling position tracking
        parser.setTrackPosition(true);
        // THEN isTrackPosition should return true
        assertTrue(parser.isTrackPosition(), "Position tracking should be enabled after setTrackPosition(true)");
    }

    @Test
    @DisplayName("TC03: htmlParser.defaultNamespace returns HTML namespace")
    public void test_TC03() {
        // GIVEN a parser created via htmlParser()
        Parser parser = Parser.htmlParser();
        // WHEN querying defaultNamespace
        String ns = parser.defaultNamespace();
        // THEN it should be the HTML namespace constant
        assertEquals(Parser.NamespaceHtml, ns, "htmlParser should default to the HTML namespace");
    }

    @Test
    @DisplayName("TC04: xmlParser.defaultNamespace returns XML namespace")
    public void test_TC04() {
        // GIVEN a parser created via xmlParser()
        Parser parser = Parser.xmlParser();
        // WHEN querying defaultNamespace
        String ns = parser.defaultNamespace();
        // THEN it should be the XML namespace constant
        assertEquals(Parser.NamespaceXml, ns, "xmlParser should default to the XML namespace");
    }

    @Test
    @DisplayName("TC05: parseBodyFragment with two top-level nodes exercises removal and append loops")
    public void test_TC05() {
        // GIVEN HTML with two sibling <p> elements
        String html = "<p>one</p><p>two</p>";
        String baseUri = "http://example.com";
        // WHEN parsing into a Document body fragment
        Document doc = Parser.parseBodyFragment(html, baseUri);
        // THEN both <p> elements should be present in order
        List<Element> ps = doc.body().getElementsByTag("p");
        assertEquals(2, ps.size(), "Expected two <p> elements in the body");
        assertEquals("one", ps.get(0).text(), "First paragraph text should be 'one'");
        assertEquals("two", ps.get(1).text(), "Second paragraph text should be 'two'");
    }

    @Test
    @DisplayName("TC06: unescapeEntities unescapes with inAttribute=false and with inAttribute=true")
    public void test_TC06() {
        // GIVEN input containing an HTML entity
        String input = "one &amp; two";
        // WHEN unescaping in both modes
        String out1 = Parser.unescapeEntities(input, false);
        String out2 = Parser.unescapeEntities(input, true);
        // THEN both outputs should replace '&amp;' with '&'
        assertEquals("one & two", out1, "Entity should be unescaped in text mode");
        assertEquals("one & two", out2, "Entity should be unescaped in attribute mode");
    }

    @Test
    @DisplayName("TC07: clone and newInstance create independent parser instances with same settings")
    public void test_TC07() {
        // GIVEN a parser with custom settings and position tracking enabled
        Parser p0 = Parser.htmlParser()
                .setTrackPosition(true)
                .settings(ParseSettings.preserveCase);
        // WHEN cloning and creating a new instance
        Parser p1 = p0.clone();
        Parser p2 = p0.newInstance();
        // THEN the clones should be distinct objects
        assertNotSame(p0, p1, "clone() should return a different instance");
        assertNotSame(p0, p2, "newInstance() should return a different instance");
        // AND both should preserve the trackPosition flag
        assertTrue(p1.isTrackPosition(), "Cloned parser should retain trackPosition setting");
        assertTrue(p2.isTrackPosition(), "New instance parser should retain trackPosition setting");
        // AND both should have identical settings
        assertEquals(p0.settings(), p1.settings(), "Cloned parser should preserve settings");
        assertEquals(p0.settings(), p2.settings(), "New instance parser should preserve settings");
    }

    @Test
    @DisplayName("TC08: isContentForTagData returns true for 'script' and false for 'div'")
    public void test_TC08() {
        // GIVEN an HTML parser
        Parser parser = Parser.htmlParser();
        // WHEN checking content-for-data behavior
        boolean b1 = parser.isContentForTagData("script"); // scripts should use Data node mode
        boolean b2 = parser.isContentForTagData("div");    // div is normal container
        // THEN script returns true, div returns false
        assertTrue(b1, "script should be treated as data content");
        assertFalse(b2, "div should not be treated as data content");
    }

    @Test
    @DisplayName("TC09: parseFragmentInput overloads with null context and String input trigger parseFragment")
    public void test_TC09() {
        // GIVEN an HTML parser and a simple inline fragment
        Parser parser = Parser.htmlParser();
        String frag = "<span>ok</span>";
        // WHEN calling both overloads
        List<Node> list1 = parser.parseFragmentInput(frag, null, "");
        List<Node> list2 = parser.parseFragmentInput(new StringReader(frag), null, "");
        // THEN each should yield exactly one node of type Element 'span'
        assertEquals(1, list1.size(), "Expected one node from string overload");
        assertEquals(1, list2.size(), "Expected one node from reader overload");
        Node n1 = list1.get(0);
        assertTrue(n1 instanceof Element, "Parsed node should be an Element");
        assertEquals("span", ((Element) n1).tagName(), "Element tag name should be 'span'");
    }
}