package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.nodes.TextNode;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;
public class Element_appendChild_2_Test {

    @Test
    @DisplayName("appendChild reparent of first child when oldParent has multiple children updates siblingIndex of remaining siblings")
    public void test_TC05() throws Exception {
        // GIVEN: oldParent with two children child1 and child2
        Element oldParent = new Element("div");
        Element child1 = new Element("span");
        Element child2 = new Element("p");
        oldParent.appendChild(child1);
        oldParent.appendChild(child2);
        Element newParent = new Element("section");

        // WHEN: reparent child1 into newParent
        newParent.appendChild(child1);

        // THEN: oldParent now has only one child (child2)
        assertEquals(1, oldParent.childNodeSize(), "Old parent should have one child after reparenting child1");
        // check remaining child2 siblingIndex==0 via reflection on Node.siblingIndex
        Field idxField = child2.getClass().getSuperclass().getDeclaredField("siblingIndex");
        idxField.setAccessible(true);
        int child2Index = idxField.getInt(child2);
        assertEquals(0, child2Index, "Remaining child2 should have siblingIndex 0 after child1 removed");
        // newParent has child1
        assertEquals(1, newParent.childNodeSize(), "New parent should have one child after appendChild");
        // child1's parent is newParent
        assertSame(newParent, child1.parent(), "Child1's parent should be newParent");
        // child1 siblingIndex==0
        idxField = child1.getClass().getSuperclass().getDeclaredField("siblingIndex");
        idxField.setAccessible(true);
        int child1Index = idxField.getInt(child1);
        assertEquals(0, child1Index, "Child1 should have siblingIndex 0 in newParent");
    }

    @Test
    @DisplayName("appendChild of a TextNode child on fresh element initializes childNodes and appends text node")
    public void test_TC06() throws Exception {
        // GIVEN: fresh element el and a text node
        Element el = new Element("div");
        TextNode text = new TextNode("hello");
        // WHEN: appendChild
        Element returned = el.appendChild(text);

        // THEN: returned is same element
        assertSame(el, returned, "appendChild should return the element itself");
        // element has one child
        assertEquals(1, el.childNodeSize(), "Element should have exactly one child after appendChild of text node");
        // text node's parent is el
        assertSame(el, text.parent(), "TextNode's parent should be the element after appendChild");
        // text siblingIndex==0
        Field idxField = text.getClass().getSuperclass().getDeclaredField("siblingIndex");
        idxField.setAccessible(true);
        int textIndex = idxField.getInt(text);
        assertEquals(0, textIndex, "TextNode should have siblingIndex 0 after appendChild");
    }
}