package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class Element_child_0_Test {

    @Test
    @DisplayName("child(0) on element with no children triggers IndexOutOfBoundsException (childNodeSize == 0 branch)")
    void test_TC01() {
        // GIVEN: an element with zero children, so childElementsList() is empty
        Element el = new Element("div");
        // WHEN & THEN: accessing child(0) on empty list must throw IndexOutOfBoundsException
        assertThrows(IndexOutOfBoundsException.class, () -> el.child(0));
    }

    @Test
    @DisplayName("child(0) on element with only non-Element child nodes triggers IndexOutOfBoundsException (filter loop yields empty list)")
    void test_TC02() {
        // GIVEN: an element with one TextNode child, but no Element children after filtering
        Element el = new Element("p");
        el.appendChild(new TextNode("text")); // non-Element child
        // WHEN & THEN: filtering yields empty element list, so child(0) throws IndexOutOfBoundsException
        assertThrows(IndexOutOfBoundsException.class, () -> el.child(0));
    }

    @Test
    @DisplayName("child(0) returns first element when exactly one Element child exists (loop-1 branch)")
    void test_TC03() {
        // GIVEN: parent has exactly one Element child
        Element parent = new Element("ul");
        Element childEl = new Element("li");
        parent.appendChild(childEl);
        // WHEN: retrieve child at index 0
        Element result = parent.child(0);
        // THEN: result must be the same child element instance
        assertEquals(childEl, result, "Expected the sole Element child to be returned");
    }

    @Test
    @DisplayName("child(1) returns second element when multiple Element children exist (loop-2 branch, index in middle)")
    void test_TC04() {
        // GIVEN: parent has two Element children in order [first, second]
        Element parent = new Element("div");
        Element first = new Element("span");
        Element second = new Element("b");
        parent.appendChild(first);
        parent.appendChild(second);
        // WHEN: retrieve child at index 1 (second element)
        Element result = parent.child(1);
        // THEN: result must be the second element added
        assertEquals(second, result, "Expected the second Element child to be returned");
    }

    @Test
    @DisplayName("child(-1) with negative index throws IndexOutOfBoundsException (get negative index)")
    void test_TC05() {
        // GIVEN: parent has one Element child, but negative index is invalid
        Element parent = new Element("div");
        Element childEl = new Element("i");
        parent.appendChild(childEl);
        // WHEN & THEN: child(-1) is out of valid range [0, size-1], so throws IndexOutOfBoundsException
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(-1));
    }
}