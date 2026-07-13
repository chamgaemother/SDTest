package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
public class Element_appendChild_2_Test {

    @Test
    @DisplayName("TC06: appendChild(sameChild) re-parents a node into its own parent when called twice (reparentChild oldParent == this, ensureChildNodes branch false)")
    void test_TC06() throws Exception {
        // GIVEN a parent with one existing child
        Element parent = new Element("div");
        TextNode child = new TextNode("txt");
        // First append to establish child.parentNode == parent and initialize childNodes (ensureChildNodes creates list)
        Element firstResult = parent.appendChild(child);
        assertSame(parent, firstResult, "First appendChild should return the parent element");
        assertEquals(1, parent.childNodeSize(), "After first append, childNodeSize should be 1");
        // WHEN appendChild is called again with the same child
        // This should trigger reparenting: oldParent == this, so reparentChild branch executed,
        // and ensureChildNodes should find childNodes already != EmptyNodes (branch false)
        Element result = parent.appendChild(child);

        // THEN no exception is thrown and result is parent
        assertSame(parent, result, "Second appendChild of same child should return the parent element");
        // parent.childNodeSize() remains 1 (no duplicate)
        assertEquals(1, parent.childNodeSize(), "Appending same child twice should not increase childNodeSize");
        // child.parentNode remains parent after reparent
        // Use reflection to read protected parentNode field in Node
        Field parentField = Node.class.getDeclaredField("parentNode");
        parentField.setAccessible(true);
        Object actualParent = parentField.get(child);
        assertSame(parent, actualParent, "Child's parentNode should remain the parent element after reparenting");

        // siblingIndex field should be reset to 0 by setSiblingIndex call after add
        Field siblingIndexField = Node.class.getDeclaredField("siblingIndex");
        siblingIndexField.setAccessible(true);
        int idx = siblingIndexField.getInt(child);
        assertEquals(0, idx, "Child's siblingIndex should be reset to 0 after reparenting into same parent");
    }
}