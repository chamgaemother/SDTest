package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Importing the required classes from jsoup
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.Element; // Added import for Element

public class Element_appendChild_0_Test {

    @Test
    @DisplayName("appendChild throws IllegalArgumentException when child is null (Validate.notNull)")
    public void test_TC01() {
        // GIVEN: a fresh Element with no children
        org.jsoup.nodes.Element parent = new org.jsoup.nodes.Element("div"); // Fully qualified name
        Node child = null;
        // WHEN / THEN: passing null should hit the Validate.notNull check and throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> parent.appendChild(child));
    }

    @Test
    @DisplayName("appendChild on empty children list creates childNodes and adds child at index 0 (ensureChildNodes branch true)")
    public void test_TC02() {
        // GIVEN: an element with no childNodes (childNodes == EmptyNodes)
        org.jsoup.nodes.Element parent = new org.jsoup.nodes.Element("span"); // Fully qualified name
        TextNode child = new TextNode("text");
        // Precondition: childNodes is the sentinel empty list, so ensureChildNodes() will allocate a new NodeList

        // WHEN: appendChild should allocate childNodes list and add the child as first element
        parent.appendChild(child);

        // THEN: one child node exists
        assertEquals(1, parent.childNodeSize(), "Expected exactly one child after append on empty list");
        // THEN: the child's siblingIndex must be 0
        assertEquals(0, child.getSiblingIndex(), "Child should have sibling index 0 when first appended");
        // THEN: the child's parent reference should point to the parent element
        assertSame(parent, child.parent(), "Child.parent() should reference the parent element");
    }

    @Test
    @DisplayName("appendChild on non-empty children list skips creation and adds child at next index (ensureChildNodes branch false)")
    public void test_TC03() {
        // GIVEN: an element and one existing child, so childNodes != EmptyNodes
        org.jsoup.nodes.Element parent = new org.jsoup.nodes.Element("p"); // Fully qualified name
        TextNode first = new TextNode("one");
        parent.appendChild(first);
        // Now childNodes is initialized and contains one element

        // WHEN: appendChild should skip re-creation and just add at the end
        TextNode newChild = new TextNode("two");
        parent.appendChild(newChild);

        // THEN: now two child nodes exist
        assertEquals(2, parent.childNodeSize(), "Expected two children after appending second child");
        // THEN: newChild should receive siblingIndex 1 (second position)
        assertEquals(1, newChild.getSiblingIndex(), "New child should have sibling index 1");
        // THEN: the new child's parent reference should point to the parent element
        assertSame(parent, newChild.parent(), "New child's parent should be the element it was appended to");
    }
}