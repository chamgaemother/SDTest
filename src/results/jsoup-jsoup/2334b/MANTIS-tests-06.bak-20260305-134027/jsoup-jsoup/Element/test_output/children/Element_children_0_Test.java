package org.jsoup.nodes;

import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class Element_children_0_Test {

    @Test
    @DisplayName("children() returns empty Elements when no child nodes (childNodeSize()==0 branch)")
    public void test_TC01() {
        // GIVEN an element with no children so childNodeSize()==0 triggers the empty branch
        Element el = new Element("div");
        // WHEN
        Elements result = el.children();
        // THEN expect empty list
        assertEquals(0, result.size(), "Expected no children when none were added");
    }

    @Test
    @DisplayName("children() filters non-Element child (only one TextNode yields empty list)")
    public void test_TC02() {
        // GIVEN an element with a single TextNode child, so childElementsList iterates 1 node and filters out as non-Element
        Element el = new Element("p");
        el.appendChild(new TextNode("text"));
        // WHEN
        Elements result = el.children();
        // THEN expect empty list since text nodes are not elements
        assertEquals(0, result.size(), "Expected no Element children when only TextNode child present");
    }

    @Test
    @DisplayName("children() returns only Element children filtering out non-Element nodes (mixed nodes)")
    public void test_TC03() {
        // GIVEN mixed child nodes: TextNode, Element span, Comment -> only the span should be returned
        Element el = new Element("div");
        el.appendChild(new TextNode("t"));                // non-Element, should be filtered
        el.appendChild(new Element("span"));              // Element, should be included
        el.appendChild(new Comment("c"));                 // non-Element, should be filtered
        // WHEN
        Elements result = el.children();
        // THEN only one child element 'span'
        assertEquals(1, result.size(), "Expected exactly one Element child");
        assertEquals("span", result.get(0).tagName(), "Expected the child tagName to be 'span'");
    }

    @Test
    @DisplayName("children() uses cached shadowChildrenRef on second call (cache hit branch)")
    public void test_TC04() {
        // GIVEN an element that will build its shadowChildrenRef on first call
        Element el = new Element("div");
        el.appendChild(new Element("a"));                  // first element child
        el.appendChild(new Element("b"));                  // second element child
        // WHEN first and second calls
        Elements first = el.children();                      // builds cache
        Elements second = el.children();                     // should hit cache branch and reuse list instance
        // THEN first contains two entries and second should be same instance (cache used)
        assertEquals(2, first.size(), "First call should have two children");
        assertSame(first, second, "Second call should return the same Elements instance from cache");
    }

    @Test
    @DisplayName("children() returns independent Elements instance each call but same content when cache cleared by mutation")
    public void test_TC05() {
        // GIVEN an element with one child element 'x' that builds cache
        Element el = new Element("div");
        Element c1 = new Element("x");
        el.appendChild(c1);
        Elements list1 = el.children();                      // cache contains [x]
        // trigger cache invalidation by mutating the child nodes
        el.appendChild(new Element("y"));                  // adds new child and calls nodelistChanged
        // WHEN calling children again after mutation
        Elements list2 = el.children();                      // rebuilds cache, now [x, y]
        // THEN list1 remains size 1 and list2 size 2 with new element 'y'
        assertEquals(1, list1.size(), "Original list1 should remain with one child 'x'");
        assertEquals(2, list2.size(), "After mutation, list2 should have two children");
        assertEquals("y", list2.get(1).tagName(), "Second child in list2 should be 'y'");
    }
}