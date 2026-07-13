package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Element_appendChild_0_Test {

    @Test
    @DisplayName("appendChild(null) throws NullPointerException when child is null")
    public void test_TC01() {
        // Given an Element parent and a null child, the method should throw NPE before any mutation (branch B1 true)
        Element parent = new Element("div");
        Node child = null;
        assertThrows(NullPointerException.class, () -> parent.appendChild(child));
    }

    @Test
    @DisplayName("appendChild(child) on empty parent initializes childNodes and adds child at index 0")
    public void test_TC02() throws Exception {
        // Given an Element with no children (childNodes==EmptyNodes triggers ensureChildNodes), and a TextNode child
        Element parent = new Element("ul");
        TextNode child = new TextNode("text");
        // When
        parent.appendChild(child);
        // Then: childNodes list should now contain the child at index 0, and siblingIndex updated
        Field nodesField = Element.class.getDeclaredField("childNodes");
        nodesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Node> nodes = (List<Node>) nodesField.get(parent);
        assertEquals(1, parent.childNodeSize(), "Expected one child node after append");
        assertSame(child, nodes.get(0), "Expected appended child at index 0");
        assertEquals(0, child.siblingIndex(), "Expected child siblingIndex to be 0");
    }

    @Test
    @DisplayName("appendChild(child) when parent already has one child reuses existing childNodes and adds second child at index 1")
    public void test_TC03() throws Exception {
        // Given a parent with one existing child, appending a second should add at index 1 without reinitializing the list
        Element parent = new Element("p");
        TextNode c1 = new TextNode("a");
        TextNode c2 = new TextNode("b");
        parent.appendChild(c1); // first append
        // precondition ensures childNodes is non-empty now (branch-false)
        parent.appendChild(c2);
        // Then: list size 2, second element c2, siblingIndex 1
        Field nodesField = Element.class.getDeclaredField("childNodes");
        nodesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Node> nodes = (List<Node>) nodesField.get(parent);
        assertEquals(2, parent.childNodeSize(), "Expected two child nodes after second append");
        assertSame(c2, nodes.get(1), "Expected second appended child at index 1");
        assertEquals(1, c2.siblingIndex(), "Expected second child siblingIndex to be 1");
    }

    @Test
    @DisplayName("appendChild(child) moves child from old parent to new parent and updates sibling indices")
    public void test_TC04() throws Exception {
        // Given a child already appended to oldParent, appending it to newParent should reparent it
        Element oldParent = new Element("div");
        Element newParent = new Element("section");
        Element child = new Element("span");
        oldParent.appendChild(child); // child.siblingIndex == 0 under oldParent
        // Reparent: branch-false path ensures ensureChildNodes for newParent
        newParent.appendChild(child);
        // Then: oldParent should have no children, newParent has child at index 0, child's parent updated, siblingIndex 0
        Field nodesField = Element.class.getDeclaredField("childNodes");
        nodesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Node> oldNodes = (List<Node>) nodesField.get(oldParent);
        @SuppressWarnings("unchecked")
        List<Node> newNodes = (List<Node>) nodesField.get(newParent);
        assertEquals(0, oldParent.childNodeSize(), "Expected old parent to have zero children after reparenting");
        assertEquals(1, newParent.childNodeSize(), "Expected new parent to have one child after reparenting");
        assertSame(child, newNodes.get(0), "Expected reparented child in new parent's nodes");
        assertSame(newParent, child.parent(), "Expected child's parent reference updated to newParent");
        assertEquals(0, child.siblingIndex(), "Expected child's siblingIndex reset to 0 under new parent");
    }
}