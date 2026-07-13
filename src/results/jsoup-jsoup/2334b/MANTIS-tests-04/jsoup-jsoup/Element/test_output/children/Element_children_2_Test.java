package org.jsoup.nodes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.select.Elements;

/**
 * JUnit 5 tests for Element.children() caching and invalidation behavior.
 */
public class Element_children_2_Test {

    @Test
    @DisplayName("children() returns cached Elements list on second call when multiple Element children exist")
    public void test_TC09() {
        // GIVEN: an Element with three child elements appended
        org.jsoup.nodes.Element el = new org.jsoup.nodes.Element("div");
        el.appendElement("a"); // childElementsList() will include this element
        el.appendElement("b"); // second child
        el.appendElement("c"); // third child
        // First call populates cache (shadowChildrenRef)
        Elements first = el.children();
        assertEquals(3, first.size(), "First children() call should return 3 elements");

        // WHEN: calling children() again should hit the cache (no rebuild)
        Elements second = el.children();

        // THEN: same instance is returned and size remains 3
        assertSame(first, second, "Second children() call should return the same cached Elements instance");
        assertEquals(3, second.size(), "Cached children list should still have 3 elements");
    }

    @Test
    @DisplayName("children() rebuilds and returns empty Elements after all children removed and cache invalidated")
    public void test_TC10() {
        // GIVEN: an Element with one child element appended and children() called to populate cache
        org.jsoup.nodes.Element el = new org.jsoup.nodes.Element("div");
        el.appendElement("span"); // one child
        Elements first = el.children();
        assertEquals(1, first.size(), "Initial children() call should return 1 element");

        // Empty the element: should clear childNodes and invalidate shadowChildrenRef cache
        el.empty(); // triggers nodelistChanged via NodeList.onContentsChanged

        // WHEN: calling children() after empty should rebuild and return empty list
        Elements second = el.children();

        // THEN: new list of size 0 is returned
        assertEquals(0, second.size(), "After empty(), children() should rebuild and return an empty list");
    }
}