package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.Comment;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Element_children_0_Test {

    @Test
    @DisplayName("childNodeSize == 0 returns an empty Elements list without iterating loop")
    void test_TC01() {
        // GIVEN an element with no children: childNodeSize == 0 triggers the empty-case shortcut
        Element el = new Element("div");
        // WHEN
        Elements result = el.children();
        // THEN should return an empty list
        assertTrue(result.isEmpty(), "Expected no child elements when none have been added");
    }

    @Test
    @DisplayName("childNodeSize > 0 with non-Element children returns empty but still iterates loop")
    void test_TC02() {
        // GIVEN an element with one TextNode child: childNodeSize > 0 and only non-Element nodes
        Element el = new Element("div");
        el.appendChild(new TextNode("text")); // forces loop iteration but no element to add
        // WHEN
        Elements result = el.children();
        // THEN still returns empty list since no Element children
        assertTrue(result.isEmpty(), "Expected no child elements when only text nodes are present");
    }

    @Test
    @DisplayName("childNodeSize > 0 with one Element child returns list of one Element and caches it")
    void test_TC03() {
        // GIVEN an element with one Element child: loop adds exactly that element
        Element el = new Element("ul");
        Element li = new Element("li");
        el.appendChild(li);
        // WHEN
        Elements result = el.children();
        // THEN should contain exactly the added element
        assertEquals(1, result.size(), "Expected exactly one child element");
        assertSame(li, result.get(0), "Expected the list to contain the same Element instance appended");
    }

    @Test
    @DisplayName("subsequent call uses cached shadowChildrenRef path and does not re-compute filter")
    void test_TC04() {
        // GIVEN an element with one Element child, and cache populated by first children() call
        Element el = new Element("ul");
        Element li = new Element("li");
        el.appendChild(li);
        Elements first = el.children(); // populate cache
        // WHEN calling children() a second time: should hit cache path, returning same instance
        Elements second = el.children();
        // THEN the returned instance should be identical to the first
        assertSame(first, second, "Expected second call to children() to return cached Elements instance");
    }

    @Test
    @DisplayName("childNodeSize > 0 with mixed Node types returns only Element children in order")
    void test_TC05() {
        // GIVEN an element with mixed children: text, element, comment, element
        Element el = new Element("div");
        TextNode n1 = new TextNode("t1"); el.appendChild(n1);
        Element e1 = new Element("span"); el.appendChild(e1);
        Comment c = new Comment("c"); el.appendChild(c);
        Element e2 = new Element("b"); el.appendChild(e2);
        // WHEN
        Elements result = el.children();
        // THEN only the Element instances in original order should appear
        assertEquals(2, result.size(), "Expected two child elements filtered from mixed nodes");
        assertSame(e1, result.get(0), "First filtered child should be the <span> element");
        assertSame(e2, result.get(1), "Second filtered child should be the <b> element");
    }
}