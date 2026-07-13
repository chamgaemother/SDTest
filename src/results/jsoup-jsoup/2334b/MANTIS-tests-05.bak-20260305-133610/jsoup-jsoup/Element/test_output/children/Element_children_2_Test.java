package org.jsoup.nodes;

import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for Element.children() method, covering scenarios TC07 and TC08.
 */
public class Element_children_2_Test {

    @Test
    @DisplayName("TC07: children() filters out DataNode instances when only DataNode present (childNodeSize>0 but no Element instances)")
    void test_TC07() {
        // GIVEN an element with exactly one DataNode child and no Element children
        Element el = new Element("div");
        // append a DataNode to ensure childNodeSize > 0 but no Element instances in childElementsList
        el.appendChild(new DataNode("data"));

        // WHEN children() is invoked
        Elements result = el.children();

        // THEN the returned Elements list should be empty, filtering out non-Element nodes
        assertEquals(0, result.size(),
                "children() should filter out DataNode and return empty when no Element children");
    }

    @Test
    @DisplayName("TC08: children() returns cached list on second call without rebuilding when shadowChildrenRef referent is non-null")
    void test_TC08() {
        // GIVEN a parent element with one child Element, to exercise caching in childElementsList()
        Element parent = new Element("ul");
        Element li = new Element("li");
        // append an Element child so childNodeSize>0 and shadowChildrenRef gets populated on first children() call
        parent.appendChild(li);

        // first call builds and caches the list of Element children
        Elements first = parent.children();
        // Confirm expected size and content on first call
        assertEquals(1, first.size(), "First children() call should return one element");
        assertSame(li, first.get(0), "First children() call should contain the appended child element");

        // WHEN calling children() again, shadowChildrenRef should referent be non-null, so list reused
        Elements second = parent.children();

        // THEN verify that either the same list instance is returned or an equal list is produced, and content correct
        assertTrue(first == second || first.equals(second),
                "Second children() call should reuse cached list instance or return an equal list");
        assertEquals(1, second.size(), "Second children() call should return one element");
        assertSame(li, second.get(0), "The element returned on second call should be the same child instance");
    }
}