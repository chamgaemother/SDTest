package org.jsoup.nodes;

import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JUnit 5 tests for Element.children() method, covering ordering, filtering, and iteration.
 */
public class Element_children_2_Test {

    @Test
    @DisplayName("children() after interleaved prependElement and appendElement returns correct ordered list")
    public void test_TC09() {
        // GIVEN a fresh parent element with no children
        Element parent = new Element("div");
        // WHEN interleaving prependElement and appendElement to force two branches: ensureChildNodes path and childElementsList filtering
        Element p = parent.appendElement("p");
        // prependElement should insert at index 0, testing the negative index adjusted path in prepend
        Element span = parent.prependElement("span");
        // THEN children() should return only Element nodes in the order [span, p]
        Elements result = parent.children();
        // Expect two element children, first is span, then p
        assertEquals(2, result.size(), "Expected two child elements after interleaved prepend and append");
        assertEquals("span", result.get(0).tagName(), "First child should be <span>");
        assertEquals("p",    result.get(1).tagName(), "Second child should be <p>");
    }

    @Test
    @DisplayName("children() after bulk appendChildren and prependChildren returns only Element children in correct sequence")
    public void test_TC10() {
        // GIVEN a fresh parent and mixed-node lists to prepend/append, covering bulk insert negative index and positive index paths
        Element parent = new Element("div");
        // before list: a TextNode (should be filtered out) and an Element <b>
        List<Node> before = Arrays.asList(
            new TextNode("t"),
            new Element(Tag.valueOf("b", Parser.NamespaceHtml, ParseSettings.preserveCase), null)
        );
        // after list: an Element <p>, a TextNode (filtered), and an Element <em>
        List<Node> after = Arrays.asList(
            new Element(Tag.valueOf("p", Parser.NamespaceHtml, ParseSettings.preserveCase), null),
            new TextNode("x"),
            new Element(Tag.valueOf("em", Parser.NamespaceHtml, ParseSettings.preserveCase), null)
        );
        // WHEN inserting before and after
        parent.prependChildren(before);
        parent.appendChildren(after);
        // THEN children() returns only Elements in sequence: b (from before), then p and em (from after)
        Elements result = parent.children();
        assertEquals(3, result.size(), "Expected three element children after bulk prepend and append");
        assertEquals("b",  result.get(0).tagName(), "First child should be <b>");
        assertEquals("p",  result.get(1).tagName(), "Second child should be <p>");
        assertEquals("em", result.get(2).tagName(), "Third child should be <em>");
    }
}