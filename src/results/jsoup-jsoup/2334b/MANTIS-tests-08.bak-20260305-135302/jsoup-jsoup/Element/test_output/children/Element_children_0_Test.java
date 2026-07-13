package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class Element_children_0_Test {

    @Test
    @DisplayName("TC01: children() on element with no child nodes returns empty list (childNodeSize()==0 short-circuit)")
    public void test_TC01() {
        // GIVEN an element with no children: childNodeSize()==0 triggers short-circuit B1→B1→B2
        Element el = new Element("div");
        // WHEN
        Elements result = el.children();
        // THEN
        assertEquals(0, result.size(), "Expected no children when element has no child nodes");
    }

    @Test
    @DisplayName("TC02: children() filters out non-Element nodes: mixed TextNode and Element yields only Element")
    public void test_TC02() {
        // GIVEN an element with one TextNode and one Element child: childNodeSize()>0 enters loop B1→B2
        Element el = new Element("div");
        el.appendChild(new TextNode("text")); // non-Element node should be skipped
        el.appendChild(new Element("span"));   // one Element node to include
        // WHEN
        Elements result = el.children();
        // THEN loop runs once for each child; only the Element node is collected (loop×1 → B3)
        assertEquals(1, result.size(), "Expected only one element child");
        assertEquals("span", result.get(0).tagName(), "Expected the single child to be <span>");
    }

    @Test
    @DisplayName("TC03: children() with multiple Element children returns all in order (loop N>1)")
    public void test_TC03() {
        // GIVEN an element with two Element children: childNodeSize()>0 and loop runs twice B1→B2(loop×2)→B3
        Element el = new Element("ul");
        el.appendChild(new Element("li"));
        el.appendChild(new Element("li"));
        // WHEN
        Elements result = el.children();
        // THEN both <li> elements are returned in insertion order
        assertEquals(2, result.size(), "Expected two element children");
        assertEquals("li", result.get(0).tagName(), "First child should be <li>");
        assertEquals("li", result.get(1).tagName(), "Second child should be <li>");
    }

    @Test
    @DisplayName("TC04: children() respects shadowChildrenRef cache: after first call and then mutation, cache is invalidated and rebuilt")
    public void test_TC04() {
        // GIVEN an element with one <p> child: first call builds cache via B1→B2(loop×1)→B3
        Element el = new Element("div");
        el.appendChild(new Element("p"));
        // WHEN - first call
        Elements first = el.children();
        // THEN first result has one <p>
        assertEquals(1, first.size(), "First children() call should return one <p>");
        assertEquals("p", first.get(0).tagName(), "First child should be <p>");

        // WHEN - mutate by adding a <span> child, invalidating shadowChildrenRef cache
        el.appendChild(new Element("span"));
        // second call should rebuild list: B1→B2(loop×2)→B3
        Elements second = el.children();
        // THEN second result has two children <p> and <span> in order
        assertEquals(2, second.size(), "After mutation, children() should return two elements");
        assertEquals("p", second.get(0).tagName(), "First child remains <p>");
        assertEquals("span", second.get(1).tagName(), "Second child should be <span>");
    }
}