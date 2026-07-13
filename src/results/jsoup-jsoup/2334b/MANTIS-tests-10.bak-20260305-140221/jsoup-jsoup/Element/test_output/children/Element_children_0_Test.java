package org.jsoup.nodes;

import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Element_children_0_Test {

    @Test
    @DisplayName("children() returns empty Elements when no child nodes (childNodeSize==0 branch)")
    public void test_TC01() {
        // GIVEN a newly created element with no children (childNodeSize == 0 triggers EmptyChildren path)
        Element el = new Element("div");
        // WHEN children() is called
        Elements result = el.children();
        // THEN result should be empty
        assertEquals(0, result.size(), "Expected no child elements");
    }

    @Test
    @DisplayName("children() filters out non-Element nodes and returns single Element child (one iteration of loop)")
    public void test_TC02() {
        // GIVEN an element with one TextNode and one Element child (loop runs once for non-Element, once for Element)
        Element parent = new Element("div");
        parent.appendText("text");
        Element child = new Element("span");
        parent.appendChild(child);
        // WHEN children() is called
        Elements result = parent.children();
        // THEN only the actual Element child should be present
        assertEquals(1, result.size(), "Expected exactly one child element");
        assertSame(child, result.get(0), "The returned element should be the appended child");
    }

    @Test
    @DisplayName("children() returns all Element children in order when multiple childNodes (multiple iterations)")
    public void test_TC03() {
        // GIVEN an element with mixed nodes: TextNode, Element e1, TextNode, Element e2, Element e3
        Element p = new Element("p");
        p.appendChild(new TextNode("A"));
        Element e1 = new Element("a"); p.appendChild(e1);
        p.appendText("B");
        Element e2 = new Element("b"); p.appendChild(e2);
        Element e3 = new Element("c"); p.appendChild(e3);
        // WHEN children() is called
        Elements result = p.children();
        // THEN result should contain exactly [e1, e2, e3] in order
        assertEquals(3, result.size(), "Expected three child elements");
        assertSame(e1, result.get(0), "First child should be e1");
        assertSame(e2, result.get(1), "Second child should be e2");
        assertSame(e3, result.get(2), "Third child should be e3");
    }

    @Test
    @DisplayName("children() uses cached shadowChildrenRef on second call (cache true branch)")
    @SuppressWarnings("unchecked")
    public void test_TC04() throws Exception {
        // GIVEN an element with two Element children (cache built on first call)
        Element el = new Element("div");
        Element c1 = new Element("p"); el.appendChild(c1);
        Element c2 = new Element("span"); el.appendChild(c2);
        // use reflection to call package-private childElementsList()
        Method childList = Element.class.getDeclaredMethod("childElementsList");
        childList.setAccessible(true);
        // WHEN invoked twice
        List<Element> first = (List<Element>) childList.invoke(el);
        List<Element> second = (List<Element>) childList.invoke(el);
        // THEN the same cached list instance is returned
        assertSame(first, second, "Expected childElementsList to return cached list on second call");
    }

    @Test
    @DisplayName("children() rebuilds cache after nodelistChanged clears shadowChildrenRef")
    @SuppressWarnings("unchecked")
    public void test_TC05() throws Exception {
        // GIVEN an element with one child, cache built, then a prependChild triggers nodelistChanged to clear cache
        Element el = new Element("div");
        Element c1 = new Element("p"); el.appendChild(c1);
        Method childList = Element.class.getDeclaredMethod("childElementsList");
        childList.setAccessible(true);
        // build cache
        List<Element> before = (List<Element>) childList.invoke(el);
        // mutation: prependChild clears shadowChildrenRef (nodelistChanged)
        el.prependChild(new Element("span"));
        // WHEN childElementsList is called again
        List<Element> after = (List<Element>) childList.invoke(el);
        // THEN a new list instance should be returned and contain two elements
        assertNotSame(before, after, "Expected childElementsList to rebuild cache after mutation");
        assertEquals(2, after.size(), "Expected two child elements after adding another");
    }

    @Test
    @DisplayName("children() returns empty list even after attributes-only mutation (no childNodes change)")
    public void test_TC06() {
        // GIVEN an element with no children but with attributes set (attributes-only should not affect children)
        Element el = new Element("div");
        el.attr("class", "x");
        // WHEN children() is called
        Elements result = el.children();
        // THEN result should still be empty
        assertTrue(result.isEmpty(), "Expected no child elements despite attribute changes");
    }
}