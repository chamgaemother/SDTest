package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.DataNode;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
public class Element_children_0_Test {

    @Test
    @DisplayName("TC01: children() on an element with no child nodes returns an empty Elements (childNodeSize=0 branch)")
    public void test_TC01() {
        // B0→B1→B2: no children present
        Element el = new Element("div"); // no child nodes
        Elements result = el.children();
        // assert empty as childNodeSize==0 triggers early return
        assertTrue(result.isEmpty(), "Expected no children for new element");
    }

    @Test
    @DisplayName("TC02: children() with only a TextNode child returns empty Elements (filters out non-Element)")
    public void test_TC02() {
        // B0→B1→B3(loop×1): loop sees one node but filters as it's TextNode
        Element el = new Element("p");
        el.appendChild(new TextNode("text"));
        Elements result = el.children();
        // no Element children expected
        assertTrue(result.isEmpty(), "Expected text-only child to be filtered out");
    }

    @Test
    @DisplayName("TC03: children() with one direct Element child returns list of size 1 (single iteration branch)")
    public void test_TC03() {
        // B0→B1→B3(loop×1): sees one Element child
        Element parent = new Element("ul");
        Element child = new Element("li");
        parent.appendChild(child);
        Elements result = parent.children();
        assertEquals(1, result.size(), "Expected exactly one child element");
        assertSame(child, result.get(0), "Expected returned child to be the one appended");
    }

    @Test
    @DisplayName("TC04: children() with multiple Element children returns list preserving insertion order (multiple iterations)")
    public void test_TC04() {
        // B0→B1→B3(loop×3): three Element children in order
        Element parent = new Element("div");
        Element c1 = new Element("span");
        Element c2 = new Element("a");
        Element c3 = new Element("b");
        parent.appendChild(c1);
        parent.appendChild(c2);
        parent.appendChild(c3);
        Elements result = parent.children();
        assertEquals(3, result.size(), "Expected three children elements");
        assertSame(c1, result.get(0), "First child should be c1");
        assertSame(c2, result.get(1), "Second child should be c2");
        assertSame(c3, result.get(2), "Third child should be c3");
    }

    @Test
    @DisplayName("TC05: children() filters out mixed node types returning only Element children (mixed Element, TextNode, DataNode)")
    public void test_TC05() {
        // B0→B1→B3(loop×4): nodes include TextNode, Element e1, DataNode, Element e2
        Element el = new Element("div");
        el.appendChild(new TextNode("one"));
        Element e1 = new Element("p"); el.appendChild(e1);
        el.appendChild(new DataNode("data"));
        Element e2 = new Element("span"); el.appendChild(e2);
        Elements result = el.children();
        assertEquals(2, result.size(), "Expected only two Element children");
        assertSame(e1, result.get(0), "First actual child should be e1");
        assertSame(e2, result.get(1), "Second actual child should be e2");
    }

    @Test
    @DisplayName("TC06: children() returns only direct children, not grandchildren (nested children are excluded)")
    public void test_TC06() {
        // B0→B1→B3(loop×1): only direct child c, gc is nested so ignored
        Element root = new Element("div");
        Element c = new Element("section");
        Element gc = new Element("article"); c.appendChild(gc);
        root.appendChild(c);
        Elements result = root.children();
        assertEquals(1, result.size(), "Expected only direct child");
        assertSame(c, result.get(0), "Returned child should be the section element");
    }

    @Test
    @DisplayName("TC07: children() returned Elements is a copy so modifying it does not affect the element’s internal child list")
    public void test_TC07() {
        // B0→B1→B3(loop×1): one Element child
        Element parent = new Element("div");
        Element c = new Element("span"); parent.appendChild(c);
        Elements result = parent.children();
        // modify returned list only
        result.add(new Element("p"));
        // original children should remain unchanged
        assertEquals(1, parent.children().size(), "Parent's child list should not be affected by modifying returned list");
    }

    @Test
    @DisplayName("TC08: children() reflects updates: after appending a new Element child, children() returns updated list")
    public void test_TC08() {
        // B0→B1→B2→B1→B3(loop×1): no children then one child after append
        Element el = new Element("div");
        Element e = new Element("p");
        el.appendChild(e);
        Elements result = el.children();
        assertEquals(1, result.size(), "After appending, children() should reflect new child");
        assertSame(e, result.get(0), "Returned element should be the newly appended one");
    }
}