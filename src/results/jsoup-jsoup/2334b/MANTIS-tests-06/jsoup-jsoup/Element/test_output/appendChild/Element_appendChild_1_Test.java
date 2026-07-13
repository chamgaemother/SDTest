package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Element_appendChild_1_Test {

    @Test
    @DisplayName("TC04: appendChild moves a node from old parent to new parent (reparentChild path)")
    public void test_TC04() {
        // GIVEN: an original parent with a child appended
        Element originalParent = new Element("div");
        TextNode child = new TextNode("moved");
        // initial append: child.parent != null after this -> triggers reparentChild in next append
        originalParent.appendChild(child);
        assertEquals(1, originalParent.childNodeSize(), 
            "Precondition: child should be in originalParent before moving");

        // GIVEN: a new parent element
        Element newParent = new Element("section");

        // WHEN: appending the same child to newParent -> should remove from originalParent (reparentChild)
        newParent.appendChild(child);
        // THEN: originalParent.childNodeSize() == 0 (child removed)
        assertEquals(0, originalParent.childNodeSize(), 
            "Child should be removed from the original parent upon reparenting");
        // THEN: newParent.childNodeSize() == 1 (child added)
        assertEquals(1, newParent.childNodeSize(), 
            "Child should be added to the new parent");
        // THEN: newParent.childNode(0) == child
        assertSame(child, newParent.childNode(0), 
            "The moved child should be exactly the same instance");
        // THEN: siblingIndex reset to 0
        assertEquals(0, child.siblingIndex(), 
            "After moving, siblingIndex should reflect new position at index 0");
    }
}