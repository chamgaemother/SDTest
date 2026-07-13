package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
public class Element_appendChild_0_Test {

    /**
     * Utility to read the private siblingIndex field from a Node instance via reflection.
     */
    private int getSiblingIndex(TextNode node) throws Exception {
        Field idxField = node.getClass().getSuperclass().getDeclaredField("siblingIndex");
        idxField.setAccessible(true);
        return idxField.getInt(node);
    }

    @Test
    @DisplayName("TC01: appendChild(null) throws IllegalArgumentException when child is null")
    public void test_TC01() {
        // Given: a standalone parent element with no children
        Element parent = new Element("div");
        // When/Then: passing null must trigger the Validate.notNull check → IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            parent.appendChild(null);
        });
    }

    @Test
    @DisplayName("TC02: appendChild(firstChild) creates new childNodes list and sets sibling index to 0")
    public void test_TC02() throws Exception {
        // GIVEN: parent.childNodes == EmptyNodes (never had children before)
        Element parent = new Element("p");
        TextNode child = new TextNode("text");
        // Precondition: ensureChildNodes() branch is true (childNodes == EmptyNodes)
        assertEquals(0, parent.childNodeSize());
        // WHEN: append first child, triggers creation of new NodeList
        Element returned = parent.appendChild(child);
        // THEN: one child present, parent set on child, sibling index 0, and return value is parent itself
        assertAll("first-child append",
            () -> assertEquals(1, parent.childNodeSize(), "childNodes size should be 1 after first append"),
            () -> assertSame(parent, child.parent(), "child.parent should reference the parent element"),
            () -> assertEquals(0, getSiblingIndex(child), "first child's siblingIndex must be 0"),
            () -> assertSame(parent, returned, "appendChild should return the parent for chaining")
        );
    }

    @Test
    @DisplayName("TC03: appendChild(secondChild) reuses existing childNodes list and increments sibling index to 1")
    public void test_TC03() throws Exception {
        // GIVEN: a parent with one existing child → ensureChildNodes() branch is false
        Element parent = new Element("ul");
        TextNode first = new TextNode("one");
        parent.appendChild(first);
        assertEquals(1, parent.childNodeSize(), "precondition: one child present");
        // WHEN: append a second child
        TextNode second = new TextNode("two");
        parent.appendChild(second);
        // THEN: list reused (size 2), new child's siblingIndex is 1
        assertAll("second-child append",
            () -> assertEquals(2, parent.childNodeSize(), "childNodes size should be 2 after second append"),
            () -> assertSame(parent, second.parent(), "second.parent should reference the parent element"),
            () -> assertEquals(1, getSiblingIndex(second), "second child's siblingIndex must be 1")
        );
    }

    @Test
    @DisplayName("TC04: appendChild(reparentedChild) moves child from old parent to new parent and updates sibling index")
    public void test_TC04() throws Exception {
        // GIVEN: child already attached to oldParent (oldParent.childNodeSize()==1)
        Element oldParent = new Element("div");
        Element newParent = new Element("span");
        TextNode child = new TextNode("x");
        oldParent.appendChild(child);
        assertEquals(1, oldParent.childNodeSize(), "precondition: oldParent has one child before reparenting");
        // WHEN: append same child to newParent → reparentChild logic should remove from oldParent and add to newParent
        newParent.appendChild(child);
        // THEN: oldParent loses child, newParent gains it at index 0
        assertAll("reparent-child behavior",
            () -> assertEquals(0, oldParent.childNodeSize(), "oldParent should have 0 children after reparent"),
            () -> assertEquals(1, newParent.childNodeSize(), "newParent should have 1 child after reparent"),
            () -> assertSame(newParent, child.parent(), "child.parent must now be newParent"),
            () -> assertEquals(0, getSiblingIndex(child), "reparented child's siblingIndex must reset to 0")
        );
    }
}