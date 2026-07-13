package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Test class for Element.child(int) cache invalidation and rebuild scenario.
 */
public class Element_child_2_Test {

    @Test
    @DisplayName("After cache built, nodelistChanged invalidates shadowChildrenRef and childElementsList() recomputes on next call")
    public void test_TC09() {
        // GIVEN: a parent element and initial child to build the shadow cache
        Element parent = new Element("div");
        Element first = new Element("p");
        parent.appendChild(first);
        // Access child(0) once to build the internal childElementsList cache (shadowChildrenRef)
        Element cached = parent.child(0); // triggers cache build (B3[F], B4 loop to collect one child into cache)
        // verify we got the expected first element from cache build
        assertSame(first, cached, "Initial child should be the first element (cache build)");

        // WHEN: invalidate the cache by emptying and then append a new child
        parent.empty(); // calls nodelistChanged(), clearing shadowChildrenRef (cache invalidated)
        Element second = new Element("span");
        parent.appendChild(second); // ensure list now contains only 'second'

        // THEN: child(0) should return the newly appended element after cache invalidation and rebuild
        Element result = parent.child(0); // rebuild occurs (B6[T rebuild), B7, B8)
        assertSame(second, result, "After invalidation and new append, child(0) should be the new element");
    }
}