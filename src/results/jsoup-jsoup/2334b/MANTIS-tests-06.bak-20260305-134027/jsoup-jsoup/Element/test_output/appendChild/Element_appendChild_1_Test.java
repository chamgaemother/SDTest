package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Test class for org.jsoup.nodes.Element#appendChild(Node).
 */
public class Element_appendChild_1_Test {

    @Test
    @DisplayName("Re-appending a child already in the same parent moves it to the end without changing size")
    void test_TC05() {
        // GIVEN: a parent element with one child appended
        Element parent = new Element("div");
        TextNode child = new TextNode("x");
        parent.appendChild(child); // initial append; path enters B0→B2(true)→B3→B4→B5

        // WHEN: re-append the same child to the same parent
        parent.appendChild(child);
        // - reparenting branch should detect same parent and not increase size
        // - childNodes was non-empty, so ensureChildNodes called, then add, but implementation should avoid duplicate

        // THEN: size remains 1
        assertEquals(1, parent.childNodeSize(), "Re-appending same child should not increase child count");
        // AND: the child's parent is still the same parent
        assertSame(parent, child.parent(), "Child's parent should remain the same Element instance");
        // AND: the sibling index of the child remains at 0 (only one child)
        assertEquals(0, child.siblingIndex(), "Sibling index should remain 0 for the single child after re-append");
    }
}