package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.select.Elements;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for Element.children(), focusing on cache invalidation when modCount changes.
 */
public class Element_children_2_Test {

    @Test
    @DisplayName("children() recomputes list when cachedChildren exists but modCount has changed (cache stale)")
    public void test_TC08() {
        // GIVEN an Element with two child elements, so children() will cache the list
        Element el = new Element("div");
        el.appendElement("span"); // first child, triggers ensureChildNodes and childElementsList
        el.appendElement("b");    // second child, still populates cache in childElementsList
        Elements first = el.children();
        assertEquals(2, first.size(), "Initial cache should contain two children");

        // WHEN we clear all children, modifying childNodes.modCount, but leaving cached userData intact
        el.empty(); // empties childNodes, modCount changes but cache still holds old list

        // THEN children() should detect modCount mismatch and recompute, yielding empty list
        Elements second = el.children();
        assertEquals(0, second.size(), "After empty(), children() must return an updated empty list due to stale cache");
    }
}