package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class Element_appendChild_0_Test {

    @Test
    @DisplayName("TC01: appendChild(null) throws IllegalArgumentException when child is null")
    void test_TC01() {
        Element parent = new Element("div"); // start with a fresh element
        Node child = null; // Updated import for Node
        // branch-true: child==null should trigger IllegalArgumentException in appendChild
        assertThrows(IllegalArgumentException.class, () -> parent.appendChild(child),
                "Calling appendChild with null must throw IllegalArgumentException");
        // no state mutation: parent.childNodeSize remains zero
        assertEquals(0, parent.childNodeSize(), "No child should be added on exception");
    }

    @Test
    @DisplayName("TC02: appendChild(firstChild) on empty element allocates childNodes and sets siblingIndex to 0")
    void test_TC02() {
        Element parent = new Element("span"); // childNodes == EmptyNodes initially
        org.jsoup.nodes.TextNode child = new org.jsoup.nodes.TextNode("text"); // Correct import for TextNode
        // branch-true: ensureChildNodes called since childNodes was empty
        parent.appendChild(child);

        assertAll("After appending first child",
                () -> assertEquals(1, parent.childNodeSize(), "One child node should be present"),
                () -> assertSame(parent, child.parent(), "Child.parent should reference the parent element"),
                () -> assertEquals(0, child.getSiblingIndex(), "First child should have siblingIndex 0")
        );
    }

    @Test
    @DisplayName("TC03: appendChild(secondChild) on non-empty element reuses childNodes and sets siblingIndex to existing size")
    void test_TC03() {
        Element parent = new Element("p");
        org.jsoup.nodes.TextNode first = new org.jsoup.nodes.TextNode("one");
        parent.appendChild(first);
        // now childNodes != EmptyNodes, so ensureChildNodes is false path
        org.jsoup.nodes.TextNode second = new org.jsoup.nodes.TextNode("two"); // Correct import for TextNode
        parent.appendChild(second);

        assertAll("After appending second child",
                () -> assertEquals(2, parent.childNodeSize(), "Two child nodes should be present"),
                () -> assertSame(parent, second.parent(), "Second.parent should reference the parent element"),
                () -> assertEquals(1, second.getSiblingIndex(), "Second child should have siblingIndex equal to previous size")
        );
    }

    @Test
    @DisplayName("TC04: appendChild(existingChild) moves child from old parent to new and updates siblingIndex")
    void test_TC04() {
        Element oldParent = new Element("ul");
        Element newParent = new Element("ol");
        Element child = new Element("li"); // Correct import for Element
        oldParent.appendChild(child);
        // child is attached to oldParent, oldParent.childNodeSize == 1

        // when moving, branch-true: ensureChildNodes called in newParent
        newParent.appendChild(child);

        assertAll("After moving existing child node",
                () -> assertEquals(0, oldParent.childNodeSize(), "Old parent should have no children after move"),
                () -> assertEquals(1, newParent.childNodeSize(), "New parent should have the moved child"),
                () -> assertSame(newParent, child.parent(), "Child.parent should reference newParent"),
                () -> assertEquals(0, child.getSiblingIndex(), "Moved child should become first with index 0")
        );
    }
}