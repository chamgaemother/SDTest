package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * JUnit 5 tests for org.jsoup.nodes.Element#child(int)
 */
public class Element_child_2_Test {

    @Test
    @DisplayName("child(0) on element with only non-Element children throws IndexOutOfBoundsException (childNodeSize>0 but children list empty)")
    public void test_TC09() {
        // GIVEN: an element with one child node that is a TextNode (no Element children -> children list empty)
        Element parent = new Element("div");
        parent.appendChild(new TextNode("text"));
        // childNodeSize > 0 but childElementsList is empty -> get(0) should throw
        assertThrows(IndexOutOfBoundsException.class, () -> {
            // WHEN: calling child(0)
            parent.child(0);
        });
    }

    @Test
    @DisplayName("child(2) after modifying children invalidates and rebuilds cache (shadowChildrenRef reset branch)")
    public void test_TC10() {
        // GIVEN: an element with two initial Element children, cache populated by first retrieval
        Element parent = new Element("ul");
        parent.appendChild(new Element("li"));
        Element second = parent.appendChild(new Element("li"));
        // first call populates shadowChildrenRef cache with two elements
        parent.child(1);
        // WHEN: append a third Element child, which should invalidate the shadowChildrenRef cache
        Element third = new Element("li");
        parent.appendChild(third);
        // THEN: child(2) should rebuild cache and return the newly appended third element
        Element result = parent.child(2);
        assertSame(third, result, "Expected child(2) to return the newly appended third element after cache invalidation");
    }
}