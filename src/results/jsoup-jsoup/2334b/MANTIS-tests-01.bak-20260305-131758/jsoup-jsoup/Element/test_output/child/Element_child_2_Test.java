package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * JUnit 5 tests for org.jsoup.nodes.Element.child(int index) method.
 * Covers scenarios for negative index exception and cache invalidation upon prependChild.
 */
public class Element_child_2_Test {

    @Test
    @DisplayName("TC10: child(-1) on element with two child Elements throws IndexOutOfBoundsException for negative index on non-empty filtered list")
    public void test_TC10() {
        // GIVEN: a parent Element with two Element children to create a non-empty filtered list
        Element parent = new Element("div");
        parent.appendChild(new Element("a"));
        parent.appendChild(new Element("b"));
        // WHEN & THEN: calling child with negative index should throw IndexOutOfBoundsException (no valid negative positions)
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(-1));
    }

    @Test
    @DisplayName("TC11: child(1) after prependChild invalidation rebuilds shadowChildrenRef and returns correct second Element")
    public void test_TC11() {
        // GIVEN: a parent with one Element child, building the child cache via initial call to child(0)
        Element parent = new Element("div");
        Element first = new Element("p");
        parent.appendChild(first);
        // Build initial cache: childElementsList() and shadowChildrenRef set
        Element initial = parent.child(0);
        assertSame(first, initial, "Initial child(0) should return the appended first element");

        // GIVEN: a second Element to prepend, which will invalidate existing cache in nodelistChanged()
        Element second = new Element("span");
        // WHEN: prependChild invalidates shadowChildrenRef, causing childElementsList to rebuild
        parent.prependChild(second);
        // THEN: the filtered elements list becomes [second, first], so child(1) should return the original first element
        Element result = parent.child(1);
        assertSame(first, result, "After prepending, child(1) should traverse rebuilt list and return the original first element");
    }
}