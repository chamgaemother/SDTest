package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Element_appendChild_0_Test {

    @Test
    @DisplayName("TC01: appendChild(null) throws IllegalArgumentException when child is null")
    public void test_TC01() {
        Element parent = new Element("div");
        Node child = null;
        // branch: Validate.notNull(child) should catch null input
        assertThrows(IllegalArgumentException.class, () -> parent.appendChild(child),
                "Appending null should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("TC02: appendChild(firstChild) initializes childNodes and adds one child with sibling index 0")
    public void test_TC02() {
        Element parent = new Element("div");
        Element child = new Element("span");
        // precondition: childNodes is uninitialized (EmptyNodes)
        parent.appendChild(child);
        // after append, childNodes should be initialized and contain exactly the one child
        assertEquals(1, parent.childNodeSize(), "childNodeSize should be 1 after first appendChild"); // loop 0->1
        // child(0) filters elements only, should return our child
        assertEquals(child, parent.child(0), "The child at index 0 should be the appended node");
        // siblingIndex should be set to 0 for first element
        assertEquals(0, child.siblingIndex(), "First child's siblingIndex should be 0");
    }

    @Test
    @DisplayName("TC03: appendChild(secondChild) reuses existing childNodes without re-initialization and sets sibling index to 1")
    public void test_TC03() {
        Element parent = new Element("div");
        Element first = new Element("p");
        // initialize childNodes by first append
        parent.appendChild(first);
        Element second = new Element("a");
        // branch: childNodes already initialized, so no re-initialization
        parent.appendChild(second);
        // now should have two children
        assertEquals(2, parent.childNodeSize(), "childNodeSize should be 2 after second appendChild"); // reuse list
        assertEquals(second, parent.child(1), "The second child should be at index 1");
        assertEquals(1, second.siblingIndex(), "Second child's siblingIndex should be 1");
    }

    @Test
    @DisplayName("TC04: appendChild(childWithParent) moves child from old parent to new parent and updates sibling index")
    public void test_TC04() {
        Element oldParent = new Element("ul");
        Element newParent = new Element("ol");
        Element childUnderTest = new Element("li");
        // setup: append childUnderTest to oldParent first
        oldParent.appendChild(childUnderTest);
        // precondition: oldParent has one child, newParent none
        assertEquals(1, oldParent.childNodeSize(), "Old parent should have one child before reparenting");
        // branch: reparenting should remove from oldParent and add to newParent
        newParent.appendChild(childUnderTest);
        // oldParent should now have no children
        assertEquals(0, oldParent.childNodeSize(), "Old parent should have 0 children after reparenting");
        // newParent should have exactly the moved child
        assertEquals(1, newParent.childNodeSize(), "New parent should have 1 child after appendChild");
        // child's parent should be updated to newParent
        assertEquals(newParent, childUnderTest.parent(), "Child's parent should be newParent after reparenting");
        // siblingIndex of the reparented child should be 0 in its new parent
        assertEquals(0, childUnderTest.siblingIndex(), "Reparented child's siblingIndex should be 0");
    }
}