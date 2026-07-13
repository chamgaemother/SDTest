package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class Element_children_0_Test {

    @Test
    @DisplayName("children() on element with no child nodes returns empty list (childNodeSize==0 branch)")
    public void test_TC01() {
        // GIVEN an element with no children -> childNodeSize()==0 triggers the empty-children branch
        Element el = new Element("div");
        // WHEN
        Elements result = el.children();
        // THEN
        assertEquals(0, result.size(), "Expected no child elements when none appended");
    }

    @Test
    @DisplayName("children() with one Element child returns list of that single child (single iteration, one instanceof Element)")
    public void test_TC02() {
        // GIVEN an element and one appended child element -> single loop iteration with instanceof Element true
        Element parent = new Element("div");
        Element child = parent.appendElement("span");
        // WHEN
        Elements result = parent.children();
        // THEN
        assertEquals(1, result.size(), "Expected exactly one child element in result");
        assertEquals(child, result.get(0), "The returned child should be the appended span element");
    }

    @Test
    @DisplayName("children() with multiple Element children returns list preserving insertion order (multiple iterations, all elements)")
    public void test_TC03() {
        // GIVEN an element with two appended element children -> multiple loop iterations, both instances of Element
        Element parent = new Element("div");
        Element first = parent.appendElement("a");
        Element second = parent.appendElement("b");
        // WHEN
        Elements result = parent.children();
        // THEN
        assertEquals(2, result.size(), "Expected two child elements in result");
        assertEquals(first, result.get(0), "First child should be the 'a' element");
        assertEquals(second, result.get(1), "Second child should be the 'b' element");
    }

    @Test
    @DisplayName("children() with mixed Node types filters only Element instances (TextNode and Element)")
    public void test_TC04() {
        // GIVEN an element with text, element, then text -> loop sees TextNode, Element, TextNode; only Element accepted
        Element parent = new Element("p");
        parent.appendText("text");
        Element span = parent.appendElement("span");
        parent.appendText("more");
        // WHEN
        Elements result = parent.children();
        // THEN
        assertEquals(1, result.size(), "Expected only the span element to be returned");
        assertEquals(span, result.get(0), "The single child should be the appended span element");
    }

    @Test
    @DisplayName("children() after modifying childNodes invalidates shadow cache and updates on subsequent call")
    public void test_TC05() {
        // GIVEN an element with one child -> first children() builds cache (one iteration)
        Element parent = new Element("div");
        Element first = parent.appendElement("x");
        Elements list1 = parent.children();
        // Mutate by appending another child -> should invalidate cache
        Element second = parent.appendElement("y");
        // WHEN redo children() -> second iteration should see two elements
        Elements list2 = parent.children();
        // THEN
        assertEquals(1, list1.size(), "First retrieval should have one child");
        assertEquals(2, list2.size(), "After appending, retrieval should have two children");
        assertEquals(second, list2.get(1), "Second child in the updated list should be the newly appended element");
    }
}