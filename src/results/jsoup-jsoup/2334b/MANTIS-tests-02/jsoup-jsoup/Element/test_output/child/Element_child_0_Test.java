package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for org.jsoup.nodes.Element#child(int)
 */
public class Element_child_0_Test {

    @Test
    @DisplayName("TC01: child(0) on element with no children throws IndexOutOfBoundsException (empty children list)")
    void test_TC01() {
        // GIVEN: a div element with no children (childNodes empty -> no element children)
        Element parent = new Element("div");
        int index = 0;
        // WHEN & THEN: accessing child at index 0 should fail as there are no element children
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(index));
    }

    @Test
    @DisplayName("TC02: child(0) returns the single child element when one element child is present (loop-1, element instance)")
    void test_TC02() {
        // GIVEN: a ul element with one li child (loop executes once and finds an Element)
        Element parent = new Element("ul");
        Element li = new Element("li");
        parent.appendChild(li);
        int index = 0;
        // WHEN: retrieve first element child
        Element result = parent.child(index);
        // THEN: the returned element should be the one appended, with tagName 'li'
        assertEquals("li", result.tagName());
    }

    @Test
    @DisplayName("TC03: child(0) on element with one non-Element node throws IndexOutOfBoundsException (filter yields empty)")
    void test_TC03() {
        // GIVEN: a p element with one TextNode child (loop executes but filter rejects non-Element)
        Element parent = new Element("p");
        parent.appendChild(new TextNode("text"));
        int index = 0;
        // WHEN & THEN: requesting child should throw since no Element children after filtering
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(index));
    }

    @Test
    @DisplayName("TC04: child(0) returns first element from mixed children [TextNode, Element, Element] (loop-N)")
    void test_TC04() {
        // GIVEN: a div with mixed children: TextNode (ignored), then span and a (loop finds span first)
        Element parent = new Element("div");
        parent.appendChild(new TextNode("t")); // non-element, should be skipped
        Element span = new Element("span");
        Element anchor = new Element("a");
        parent.appendChild(span);
        parent.appendChild(anchor);
        int index = 0;
        // WHEN: get first element child
        Element result = parent.child(index);
        // THEN: should return the span element
        assertEquals("span", result.tagName());
    }

    @Test
    @DisplayName("TC05: child(1) returns second element from multiple element children (loop-N, index>0)")
    void test_TC05() {
        // GIVEN: a ul with two li children (loop finds two elements, index 1 should be second)
        Element parent = new Element("ul");
        Element first = new Element("li");
        Element second = new Element("li");
        parent.appendChild(first);
        parent.appendChild(second);
        int index = 1;
        // WHEN: get second element child
        Element result = parent.child(index);
        // THEN: same instance as parent's children().get(1)
        assertSame(parent.children().get(1), result);
    }

    @Test
    @DisplayName("TC06: child(-1) throws IndexOutOfBoundsException for negative index")
    void test_TC06() {
        // GIVEN: a div with one span child (loop finds one element, but negative index is invalid)
        Element parent = new Element("div");
        parent.appendChild(new Element("span"));
        int index = -1;
        // WHEN & THEN: negative index should throw IndexOutOfBoundsException
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(index));
    }

    @Test
    @DisplayName("TC07: child(size) throws IndexOutOfBoundsException for index equal to number of element children (boundary)")
    void test_TC07() {
        // GIVEN: an ol with two li children (loop finds two elements, childrenSize() == 2)
        Element parent = new Element("ol");
        parent.appendChild(new Element("li"));
        parent.appendChild(new Element("li"));
        int index = parent.childrenSize(); // boundary index equal to size
        // WHEN & THEN: index == size should throw IndexOutOfBoundsException
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(index));
    }
}