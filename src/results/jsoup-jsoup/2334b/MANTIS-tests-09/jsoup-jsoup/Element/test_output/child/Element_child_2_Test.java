package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Element_child_2_Test {

    @Test
    @DisplayName("child(1) returns second element when mixed element and non-element childNodes are present")
    void test_TC09() {
        // GIVEN a parent with mixed children: an element, a TextNode, then another element
        Element parent = new Element("div");
        Element first = parent.appendElement("span"); // first element child
        parent.appendChild(new TextNode("foo"));      // non-element child, should be skipped
        Element second = parent.appendElement("a");   // second element child

        // WHEN we request the 1st filtered child (0-based)
        Element result = parent.child(1);

        // THEN we should get the 'second' element, not the text node
        assertSame(second, result, "Expected the second appended Element to be returned at index 1");
    }

    @Test
    @DisplayName("childElementsList cache is invalidated after mutation and new element is returned on subsequent child() call")
    void test_TC10() {
        // GIVEN a parent and one child, and we prime the element cache by calling child(0)
        Element parent = new Element("ul");
        Element first = parent.appendElement("li");
        Element cached = parent.child(0); // build and cache the childElementsList

        // WHEN we append a new element after cache built
        Element newChild = parent.appendElement("li");
        // THEN cache should be invalidated and child(1) should return the newly appended element
        Element second = parent.child(1);

        assertSame(newChild, second, "After mutation, childElementsList cache should be invalidated so new child is returned");
    }

    @Test
    @DisplayName("child(-2) on element with two element children should throw IndexOutOfBoundsException (negative index beyond filtered lower bound)")
    void test_TC11() {
        // GIVEN a parent with exactly two element children
        Element parent = new Element("div");
        parent.appendElement("p");
        parent.appendElement("span");

        // WHEN calling child(-2), which is beyond lower bound of filtered list
        // THEN an IndexOutOfBoundsException should be thrown
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(-2),
            "Accessing a negative index beyond lower bound should throw IndexOutOfBoundsException");
    }
}