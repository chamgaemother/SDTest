package org.jsoup.nodes;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * JUnit 5 tests for Element.appendChild scenarios TC05 and TC06.
 */
public class Element_appendChild_2_Test {

    @Test
    @DisplayName("appendChild on a new Node sets the child's parent and returns this element (reparentChild no-op path)")
    public void test_TC05() {
        // GIVEN: a parent Element and a TextNode child with no existing parent
        Element parent = new Element("div");
        TextNode child = new TextNode("hello");
        assertNull(child.parent(), "Precondition failed: child should have no parent");
        
        // WHEN: appendChild is called (should follow path B0->B2->B3->B5->B6, reparentChild no-op since child.parentNode==null)
        Element result = parent.appendChild(child);
        
        // THEN: the returned Element is the same parent, and child's parent set correctly, siblingIndex == 0
        assertSame(parent, result, "appendChild should return the parent element for chaining");
        assertSame(parent, child.parent(), "appendChild should set the child's parent to the given parent");
        assertEquals(0, child.siblingIndex(), "First appended child should have siblingIndex 0");
    }

    @Test
    @DisplayName("appendChild chaining supports multiple calls and updates siblingIndex incrementally")
    public void test_TC06() {
        // GIVEN: a parent Element and two TextNode children, both with no parent
        Element parent = new Element("ul");
        TextNode first = new TextNode("one");
        TextNode second = new TextNode("two");
        assertNull(first.parent(), "Precondition failed: first child should have no parent");
        assertNull(second.parent(), "Precondition failed: second child should have no parent");
        
        // WHEN: appendChild is chained twice (path B0->B2->B3->B5->B6 executed twice)
        parent.appendChild(first).appendChild(second);
        
        // THEN: first child index 0, second child index 1, and two children in parent
        assertEquals(0, first.siblingIndex(), "First appended child should have siblingIndex 0");
        assertEquals(1, second.siblingIndex(), "Second appended child should have siblingIndex 1");
        assertEquals(2, parent.childNodeSize(), "Parent should have two child nodes after two appends");
    }
}