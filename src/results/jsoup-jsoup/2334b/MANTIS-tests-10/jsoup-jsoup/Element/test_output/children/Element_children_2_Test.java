package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.select.Elements;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JUnit 5 tests for Element.children() behavior, specifically cache invalidation after empty().
 */
public class Element_children_2_Test {

    @Test
    @DisplayName("TC07: children() returns empty list after clearing existing element children, exercising EmptyChildren branch post-cache and nodelistChanged")
    public void test_TC07() {
        // GIVEN: an Element with two child Elements
        Element el = new Element("div");
        // append first child: triggers ensureChildNodes and non-zero childNodeSize
        Element childA = new Element("a");
        el.appendChild(childA); // childNodeSize > 0
        Element childB = new Element("b");
        el.appendChild(childB); // childNodeSize > 0

        // build and cache shadowChildrenRef via initial children() call
        Elements initial = el.children();
        // initial list should reflect two element children
        assertEquals(2, initial.size(), "Initial children() should return two child elements and populate cache");

        // WHEN: clear all child nodes, should invoke nodelistChanged and reset cache
        el.empty(); // empties childNodes, making childNodeSize == 0, and clears shadowChildrenRef

        // THEN: children() should now return an empty list (EmptyChildren branch)
        Elements afterClear = el.children();
        assertEquals(0, afterClear.size(), "After empty(), children() should return an empty list and not reuse stale cache");
    }
}