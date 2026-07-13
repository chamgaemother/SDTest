package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for Element.child(int) method, covering scenarios TC08 and TC09.
 */
public class Element_child_2_Test {

    @Test
    @DisplayName("TC08: child(2) returns the third Element in a mix of three Elements and one TextNode, skipping non-Elements")
    void test_TC08() {
        // GIVEN: a parent with mixed child nodes: Element, TextNode, Element, Element
        Element parent = new Element("div");
        Element first = new Element("a");
        parent.appendChild(first);  // first element (index 0 in filtered list)
        parent.appendChild(new TextNode("skip"));  // non-element, should be skipped
        Element second = new Element("span");
        parent.appendChild(second); // second element (index 1 in filtered list)
        Element third = new Element("b");
        parent.appendChild(third);  // third element (index 2 in filtered list)

        // WHEN: retrieving the element at filtered index 2
        Element result = parent.child(2);

        // THEN: should return the third Element ("b"), skipping the TextNode
        assertSame(third, result, "child(2) should return the third element, ignoring non-Element nodes");
    }

    @Test
    @DisplayName("TC09: child(2) after cache invalidation builds new shadow list when a new Element is appended")
    void test_TC09() {
        // GIVEN: a parent with two Element children, cache is populated after first child() call
        Element parent = new Element("div");
        Element first = new Element("p");
        Element second = new Element("q");
        parent.appendChild(first);
        parent.appendChild(second);
        // populate shadow cache by accessing child(1)
        Element initial = parent.child(1);
        assertSame(second, initial, "child(1) should return the second element before mutation");

        // WHEN: append a new third Element, which should trigger nodelistChanged and cache invalidation
        Element third = new Element("span");
        parent.appendChild(third);
        // THEN: child(2) should rebuild the shadow list and return the newly appended element
        Element result2 = parent.child(2);
        assertSame(third, result2, "After appending, child(2) should return the newly added third element");

        // AND: accessing an out-of-bounds index should throw IndexOutOfBoundsException
        assertThrows(IndexOutOfBoundsException.class,
            () -> parent.child(3),
            "child(3) should throw IndexOutOfBoundsException when index exceeds element count");
    }
}