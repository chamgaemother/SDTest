package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Element_appendChild_2_Test {

    @Test
    @DisplayName("appendChild invalidates pre-populated shadowChildrenRef cache (nodelistChanged) on Element children list")
    public void test_TC06() throws Exception {
        // GIVEN: a parent with warmed shadowChildrenRef via children()
        Element parent = new Element("ul");
        // children() populates the internal shadow cache (shadowChildrenRef)
        parent.children();
        // childElementsList is package-private: ensure cache is non-null initially via reflection
        Method getChildElementsList = Element.class.getDeclaredMethod("childElementsList");
        getChildElementsList.setAccessible(true);
        // BEFORE appendChild: initial list is empty
        @SuppressWarnings("unchecked")
        List<Element> initialList = (List<Element>) getChildElementsList.invoke(parent);
        assertTrue(initialList.isEmpty(), "Initial childElementsList should be empty");

        Element child = new Element("li");
        // WHEN: appendChild should clear the cache and allow fresh compute
        parent.appendChild(child);

        // THEN: invoking childElementsList again yields a fresh list with exactly our child
        @SuppressWarnings("unchecked")
        List<Element> recomputed = (List<Element>) getChildElementsList.invoke(parent);
        assertEquals(1, recomputed.size(), "After appendChild, childElementsList should have size 1");
        assertSame(child, recomputed.get(0), "The single element in childElementsList must be the appended child");
    }

    @Test
    @DisplayName("appendChild reparenting from old parent with multiple children removes only that child and updates siblingIndex")
    public void test_TC07() throws Exception {
        // GIVEN: oldParent with three text children A, B (toMove), C
        Element oldParent = new Element("div");
        TextNode first = new TextNode("A");       // will be index 0
        TextNode toMove = new TextNode("B");      // will be index 1
        TextNode last = new TextNode("C");        // will be index 2
        oldParent.appendChild(first);
        oldParent.appendChild(toMove);
        oldParent.appendChild(last);
        // Validate initial childNodeSize==3
        assertEquals(3, oldParent.childNodeSize(), "oldParent should start with 3 children");

        Element newParent = new Element("span");
        // WHEN: reparent toMove into newParent
        newParent.appendChild(toMove);

        // THEN: oldParent.childNodeSize decremented by one (only B removed)
        assertEquals(2, oldParent.childNodeSize(), "oldParent should have 2 children after reparenting one");
        // The remaining nodes are A and C in correct order
        assertSame(first, oldParent.childNode(0), "First remaining child must be the original first");
        assertSame(last, oldParent.childNode(1), "Second remaining child must be the original last");

        // newParent has exactly the moved child at its index 0
        assertEquals(1, newParent.childNodeSize(), "newParent should have exactly one child after append");
        assertSame(toMove, newParent.childNode(0), "newParent's child must be the moved node");

        // siblingIndex of toMove reset to 0; siblingIndex is protected field in Node
        Field idxField = Node.class.getDeclaredField("siblingIndex");
        idxField.setAccessible(true);
        int idx = idxField.getInt(toMove);
        assertEquals(0, idx, "siblingIndex of the moved node should be reset to 0 in the new parent");
    }
}