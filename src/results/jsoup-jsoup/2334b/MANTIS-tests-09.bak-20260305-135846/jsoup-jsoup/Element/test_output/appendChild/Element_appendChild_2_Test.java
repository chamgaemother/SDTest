package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
public class Element_appendChild_2_Test {

    @Test
    @DisplayName("appendChild on a parent with two existing children sets correct sibling index 2 for the third child")
    void test_TC06() throws Exception {
        // GIVEN: a parent with two existing TextNode children -> ensures childNodes was initialized (EmptyNodes → NodeList)
        Element parent = new Element("div");
        TextNode first = new TextNode("one");
        TextNode second = new TextNode("two");
        parent.appendChild(first);   // B0->B2: ensureChildNodes creates childNodes list
        parent.appendChild(second);  // B3: add second child
        assertEquals(2, parent.childNodeSize(), "Precondition failed: should have two children");

        // WHEN: append a third TextNode
        TextNode third = new TextNode("three");
        parent.appendChild(third);   // B5: add third child

        // THEN: verify childNodeSize increased, parent relation, and siblingIndex==2
        assertEquals(3, parent.childNodeSize(), "Expected three children after append");
        assertSame(parent, third.parent(), "Third node should have parent set to 'parent'");

        // reflectively read the siblingIndex field on Node
        Field sibField = Node.class.getDeclaredField("siblingIndex");
        sibField.setAccessible(true);
        int idx = (int) sibField.get(third);
        assertEquals(2, idx, "Third child's siblingIndex should be 2");
    }

    @Test
    @DisplayName("appendChild moves an Element child from old parent with multiple children to new parent and resets its sibling index")
    void test_TC07() throws Exception {
        // GIVEN: oldParent has two element children [childA, childB] -> reparenting path B2
        Element oldParent = new Element("ul");
        Element childA = new Element("li");
        Element childB = new Element("li");
        oldParent.appendChild(childA);
        oldParent.appendChild(childB);
        assertEquals(2, oldParent.childNodeSize(), "Precondition failed: oldParent should have two children");

        // GIVEN: newParent empty
        Element newParent = new Element("ol");
        assertEquals(0, newParent.childNodeSize(), "Precondition failed: newParent should be empty");

        // WHEN: reparent childB under newParent
        newParent.appendChild(childB); // B2: reparentChild removes from old, B3->B5 adds to new

        // THEN: oldParent loses one child, newParent has one, childB.parent is newParent, siblingIndex==0
        assertEquals(1, oldParent.childNodeSize(), "oldParent should have one remaining child");
        assertEquals(1, newParent.childNodeSize(), "newParent should have one child after append");
        assertSame(newParent, childB.parent(), "childB should now have newParent as parent");

        // reflectively read the siblingIndex field on Node
        Field sibField = Node.class.getDeclaredField("siblingIndex");
        sibField.setAccessible(true);
        int newIdx = (int) sibField.get(childB);
        assertEquals(0, newIdx, "childB's siblingIndex in newParent should be reset to 0");
    }
}