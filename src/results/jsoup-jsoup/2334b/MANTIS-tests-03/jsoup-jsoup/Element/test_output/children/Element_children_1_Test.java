package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.jsoup.nodes.Element; // Added import for Element class
import org.jsoup.select.Elements; // Added import for Elements class
public class Element_children_1_Test {

    @Test
    @DisplayName("children() recomputes filtered list after childNodes change invalidates cache")
    public void test_TC06() {
        // GIVEN an element with no initial children (childNodeSize == 0)
        Element el = new Element("div");
        // WHEN first call to children(): should return empty and set shadowChildrenRef to empty list
        Elements first = el.children();
        // THEN first result is empty
        assertTrue(first.isEmpty(), "Expected no children on fresh element");

        // WHEN appendChild is called: triggers ensureChildNodes, addition and nodelistChanged -> clears cache
        el.appendChild(new Element("span"));
        // THEN on second call, children() must recompute and include the new child
        Elements second = el.children();
        assertEquals(1, second.size(), "Expected one child after appendChild");
        // And the child is a span element
        assertEquals("span", second.get(0).tagName(), "Expected appended child of tag 'span'");
    }

    @Test
    @DisplayName("children() short-circuits to empty when all elements removed, even if cache was previously populated")
    public void test_TC07() {
        // GIVEN an element with one Element child (childNodeSize > 0)
        Element el = new Element("ul");
        Element li = new Element("li");
        el.appendChild(li);
        // Populate cache by calling children(): now shadowChildrenRef != null and children list size == 1
        Elements initial = el.children();
        assertEquals(1, initial.size(), "Setup: expected one child before removal");

        // WHEN empty() is called: removes all childNodes, triggers nodelistChanged, so childNodeSize goes to 0
        el.empty();
        // THEN children() short-circuits on childNodeSize == 0 and returns EmptyChildren without looping
        Elements result = el.children();
        assertTrue(result.isEmpty(), "Expected no children after empty(), short-circuiting without loop");
    }
}