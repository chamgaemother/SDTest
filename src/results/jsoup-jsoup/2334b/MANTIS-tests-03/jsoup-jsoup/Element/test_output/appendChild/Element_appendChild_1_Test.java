package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
public class Element_appendChild_1_Test {

    @Test
    @DisplayName("appendChild returns this element (fluent API) when adding a new child")
    public void test_TC05() throws Exception {
        // GIVEN an empty parent and an orphan TextNode child
        Element parent = new Element("div"); // childNodes == EmptyNodes, so ensureChildNodes() will create a new NodeList
        TextNode child = new TextNode("hello"); // orphan has no parent

        // WHEN appendChild is called
        Element returned = parent.appendChild(child); // path: B0->B1(false initial)->B2->B3 return this

        // THEN the returned element should be the same as parent (fluent API)
        assertSame(parent, returned, "appendChild should return the parent itself");
        // AND the child should be appended at index 0
        assertEquals(1, parent.childNodeSize(), "There should be exactly one child after append");
        // AND the child's siblingIndex field should be set to 0
        Field idxField = Node.class.getDeclaredField("siblingIndex");
        idxField.setAccessible(true);
        int idx = idxField.getInt(child);
        assertEquals(0, idx, "Child siblingIndex should be 0 after first append");
    }

    @Test
    @DisplayName("appendChild same child twice moves it and re-adds, exercising reparent and ensureChildNodes both times")
    public void test_TC06() throws Exception {
        // GIVEN a parent and a child element already appended once
        Element parent = new Element("ul"); // initial empty childNodes
        Element child = new Element("li"); // orphan element
        parent.appendChild(child); // first append: childNodes was EmptyNodes->new NodeList, child added at index 0

        // WHEN appendChild is called again with the same child
        parent.appendChild(child); // reparentChild should detach then add again at index 0

        // THEN there should still be exactly one child in the parent
        assertEquals(1, parent.childNodeSize(), "Appending the same child twice should not increase child count");
        // AND the single child should still be the same instance
        assertSame(child, parent.child(0), "The child at index 0 should be the same instance after re-append");
        // AND the child's siblingIndex should be reset to 0
        Field idxField = Node.class.getDeclaredField("siblingIndex");
        idxField.setAccessible(true);
        int idx = idxField.getInt(child);
        assertEquals(0, idx, "Child siblingIndex should be reset to 0 after re-append");
    }
}