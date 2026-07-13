package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Element_appendChild_1_Test {

    @Test
    @DisplayName("appendChild accepts a TextNode and sets its siblingIndex correctly")
    void test_TC05() throws Exception {
        // GIVEN a new element with no existing children (childNodes == EmptyNodes)
        Element parent = new Element("div");
        TextNode txt = new TextNode("hello");
        // WHEN: appending a fresh TextNode triggers ensureChildNodes branch (EmptyNodes -> NodeList)
        parent.appendChild(txt);
        // THEN: childNodeSize should be 1, the list should contain exactly our TextNode, and its siblingIndex set to 0
        assertEquals(1, parent.childNodeSize(), "Expected one child after appendChild");
        // Access package-private field childNodes via reflection to verify exact instance in list
        Field nodesField = Element.class.getDeclaredField("childNodes");
        nodesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Node> nodes = (List<Node>) nodesField.get(parent);
        assertSame(txt, nodes.get(0), "The appended child should be the same TextNode instance");
        assertEquals(0, txt.siblingIndex(), "Sibling index of the sole child should be 0");
    }

    @Test
    @DisplayName("appendChild moves a TextNode from one parent to another and updates both parents and siblingIndex")
    void test_TC06() throws Exception {
        // GIVEN two separate parent elements, oldParent has one TextNode child, newParent has none
        Element oldParent = new Element("div");
        Element newParent = new Element("div");
        TextNode txt = new TextNode("world");
        // Append first to oldParent to set initial parent and siblingIndex
        oldParent.appendChild(txt);
        assertEquals(1, oldParent.childNodeSize(), "Precondition: oldParent should have one child before reparenting");

        // WHEN: appending the same TextNode to newParent triggers the reparent branch
        newParent.appendChild(txt);

        // THEN: oldParent should have no children (detached), newParent has exactly our TextNode, and its parent and index updated
        assertEquals(0, oldParent.childNodeSize(), "After reparenting, oldParent should have zero children");
        assertEquals(1, newParent.childNodeSize(), "newParent should have one child after reparenting");
        assertEquals(newParent, txt.parent(), "TextNode's parent should be newParent after reparenting");
        assertEquals(0, txt.siblingIndex(), "Sibling index in newParent should be reset to 0");
    }
}