package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * JUnit 5 tests for org.jsoup.nodes.Element.child(int) focusing on cache invalidation of shadowChildrenRef.
 */
public class Element_child_2_Test {

    @Test
    @DisplayName("TC07: child(n) after mutating children invalidates and rebuilds cached shadowChildrenRef (nodelistChanged branch)")
    void test_TC07() {
        // GIVEN: a parent element with two children, building the initial shadowChildrenRef cache via child(1)
        Element parent = new Element("div");
        Element first = parent.appendElement("p"); // first child
        Element second = parent.appendElement("span"); // second child
        // Access child(1) to populate shadowChildrenRef cache (path B0->B2->B3->B4->B6)
        Element cachedSecond = parent.child(1);
        assertSame(second, cachedSecond, "Initial child(1) should return the second appended child");

        // WHEN: appendElement adds a third child, triggering nodelistChanged and cache invalidation
        Element third = parent.appendElement("a"); // after mutation, shadowChildrenRef should be cleared

        // THEN: child(2) should rebuild the cache and return the newly appended third child (path nodelistChanged->rebuild)
        Element result = parent.child(2);
        assertSame(third, result, "After cache invalidation, child(2) should return the third appended child");
    }
}