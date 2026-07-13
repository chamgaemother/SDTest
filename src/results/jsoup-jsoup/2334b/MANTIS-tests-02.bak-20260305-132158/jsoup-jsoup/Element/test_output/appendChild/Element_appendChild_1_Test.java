package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
public class Element_appendChild_1_Test {

    @Test
    @DisplayName("appendChild(reparenting) moves a child from an existing parent to a new parent, removing it from the old and resetting siblingIndex")
    public void test_TC04() {
        // GIVEN: oldParent has one child, so ensureChildNodes path B3 (existing childNodes) is taken
        Element oldParent = new Element("div");
        Element newParent = new Element("section");
        TextNode child = new TextNode("hello");
        oldParent.appendChild(child);
        int oldSizeBefore = oldParent.childNodeSize();
        // WHEN: reparenting the same child under newParent triggers the reparent branch B2
        Element returned = newParent.appendChild(child);
        // THEN: child removed from oldParent and added to newParent with index reset
        assertEquals(oldSizeBefore - 1, oldParent.childNodeSize(),
            "Child should be removed from oldParent.childNodes after reparenting");
        assertEquals(1, newParent.childNodeSize(),
            "New parent should have exactly one child after appendChild");
        assertEquals(0, child.siblingIndex(),
            "Sibling index should be updated to newParent.childNodeSize()-1 (0)");
        assertSame(newParent, returned,
            "appendChild should return the new parent instance for chaining");
    }

    @Test
    @DisplayName("appendChild(self-chaining) returns the same parent element to allow method chaining")
    public void test_TC05() {
        // GIVEN: a fresh parent with no children triggers ensureChildNodes new allocation path B3
        Element parent = new Element("ul");
        TextNode child = new TextNode("item");
        // WHEN: appending first child (no reparenting needed, B2 no reparent)
        Element result = parent.appendChild(child);
        // THEN: returns the same parent and childNodeSize becomes 1
        assertSame(parent, result,
            "appendChild should return the same parent instance for chaining");
        assertEquals(1, parent.childNodeSize(),
            "Parent.childNodeSize should be 1 after adding a single child");
    }
}