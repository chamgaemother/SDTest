package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_tagSet_2_Test {

    @Test
    @DisplayName("clone() returns a deep copy with independent TreeBuilder and settings")
    public void test_TC07() {
        // GIVEN a parser with custom settings and trackPosition enabled
        Parser orig = Parser.htmlParser()
            .setTrackPosition(true)  // exercise setting trackPosition = true branch
            .settings(new ParseSettings(false, true)); // custom settings differing from default
        
        // WHEN clone is invoked
        Parser copy = orig.clone();

        // THEN copy is a different instance
        assertNotSame(orig, copy);
        // settings and trackPosition should be equal
        assertEquals(orig.isTrackPosition(), copy.isTrackPosition(), "trackPosition should be copied");
        assertEquals(orig.settings(), copy.settings(), "settings should be deeply copied and equal");
        // but treeBuilder instances must differ (deep copy)
        assertNotSame(orig.getTreeBuilder(), copy.getTreeBuilder(), "TreeBuilder should be a different instance");
    }

    @Test
    @DisplayName("parseInput(String) parses simple HTML into a Document with correct baseUri")
    public void test_TC08() {
        // GIVEN simple HTML with a single <p> element
        String html = "<p>Hello</p>";
        String base = "http://example.com/";
        Parser parser = Parser.htmlParser();
        
        // WHEN parseInput(String, String) is called
        Document doc = parser.parseInput(html, base);

        // THEN the document baseUri is preserved and the <p> text is parsed
        assertEquals(base, doc.baseUri(), "baseUri should be set on the parsed Document");
        Elements ps = doc.select("p");
        assertEquals(1, ps.size(), "One <p> element should be present");
        assertEquals("Hello", ps.first().text(), "The <p> element text should be 'Hello'");
    }

    @Test
    @DisplayName("parseFragmentInput(Reader, null) parses fragment without context element")
    public void test_TC09() throws Exception {
        // GIVEN a text fragment and a null context to cover parseFragmentInput Reader overload
        String frag = "text only";
        Reader rdr = new StringReader(frag);
        Parser parser = Parser.htmlParser();

        // WHEN parsing fragment with null context
        List<Node> nodes = parser.parseFragmentInput(rdr, null, "foo");

        // THEN we get a single TextNode whose toString equals the text
        assertEquals(1, nodes.size(), "Should parse into exactly one node");
        assertEquals("text only", nodes.get(0).toString(), "Node's text should match the input fragment");
    }

    @Test
    @DisplayName("static parseFragment(html, context, baseUri, errorList) collects parse errors when malformed")
    public void test_TC10() {
        // GIVEN malformed fragment and an external ParseErrorList with capacity 1
        String frag = "<p unclosed"; // unclosed tag triggers parse error
        Element ctx = new Element("div");
        ParseErrorList errs = ParseErrorList.tracking(1);

        // WHEN invoking the static parseFragment method that assigns errs to parser.errors
        List<Node> out = Parser.parseFragment(frag, ctx, "u", errs);

        // THEN the external list should have recorded one error and cloning still returns nodes
        assertEquals(1, errs.size(), "One parse error should be recorded in the provided list");
        assertTrue(out.size() >= 1, "At least one node should be returned even if fragment is malformed");
    }

    @Test
    @DisplayName("static parseXmlFragment parses an XML fragment into Element nodes")
    public void test_TC11() {
        // GIVEN a simple XML fragment <item>1</item>
        String xml = "<item>1</item>";
        String base = "";

        // WHEN parseXmlFragment is called
        List<Node> nodes = Parser.parseXmlFragment(xml, base);

        // THEN we expect exactly one Element node with tagName 'item'
        assertEquals(1, nodes.size(), "Should parse exactly one node");
        assertTrue(nodes.get(0) instanceof Element, "Node should be an Element");
        Element item = (Element) nodes.get(0);
        assertEquals("item", item.tagName(), "The element tagName should be 'item'");
    }

    @Test
    @DisplayName("static parseBodyFragment appends parsed nodes into body of a shell Document")
    public void test_TC12() {
        // GIVEN a fragment with a <span> element and a base URI
        String frag = "<span>X</span>";
        String base = "http://b/";

        // WHEN parseBodyFragment is invoked
        Document d = Parser.parseBodyFragment(frag, base);

        // THEN the returned Document's body should contain the appended <span>
        Element body = d.body();
        assertEquals(1, body.children().size(), "Body should have one child");
        Element span = body.child(0);
        assertEquals("span", span.tagName(), "Child tag should be 'span'");
        assertEquals("X", body.text(), "Body text should be 'X'");
    }

    @Test
    @DisplayName("unescapeEntities transforms HTML escapes when inAttribute=false")
    public void test_TC13() {
        // GIVEN a string with HTML entity escapes <, >, &amp;
        String input = "&lt;&gt;&amp;";

        // WHEN unescapeEntities with inAttribute=false is called
        String out = Parser.unescapeEntities(input, false);

        // THEN the escapes should be converted to literal characters <>&
        assertEquals("<>&", out, "Entities &lt;&gt;&amp; should unescape to <>&");
    }

    @Test
    @DisplayName("unescapeEntities inAttribute=true properly handles attribute escapes")
    public void test_TC14() {
        // GIVEN a string with attribute escape &quot;
        String input = "&quot;";

        // WHEN unescapeEntities with inAttribute=true is called
        String out = Parser.unescapeEntities(input, true);

        // THEN the attribute quote entity should unescape to a double-quote character
        assertEquals("\"", out, "&quot; should unescape to a double-quote when inAttribute=true");
    }
}