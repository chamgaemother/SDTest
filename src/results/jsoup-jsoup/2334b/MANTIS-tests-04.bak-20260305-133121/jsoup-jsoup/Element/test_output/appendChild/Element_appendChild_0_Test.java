package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
public class Element_appendChild_0_Test {

    @Test
    @DisplayName("appendChild(null) throws IllegalArgumentException covering Validate.notNull(child) null check")
    void test_TC01() throws Exception {
        Element parent = new Element("div");
        // Scenario: passing null should trigger Validate.notNull and throw IAE (path B0→B1→B6)
        assertThrows(IllegalArgumentException.class, () -> parent.appendChild(null));
        // No child to inspect for parent linkage since input was null
    }

    @Test
    @DisplayName("appendChild(firstChild) adds child when childNodes was empty (ensureChildNodes branch true)")
    void test_TC02() throws Exception {
        Element parent = new Element("div");
        TextNode child = new TextNode("text");
        // childNodes initially empty so ensureChildNodes creates a new list (branch true)
        Element result = parent.appendChild(child);
        // returns self
        assertEquals(parent, result, "Expected appendChild to return the parent element");
        // child added
        assertEquals(1, parent.childNodeSize(), "Expected one child node after appendChild on empty list");
        // parent linkage set
        assertEquals(parent, child.parent(), "Expected child.parent() to be the parent element");
        // siblingIndex field set to 0
        Field sibField = TextNode.class.getSuperclass().getDeclaredField("siblingIndex");
        sibField.setAccessible(true);
        int idx = sibField.getInt(child);
        assertEquals(0, idx, "Expected first child's siblingIndex to be 0");
    }

    @Test
    @DisplayName("appendChild(secondChild) adds child when childNodes non-empty (ensureChildNodes branch false)")
    void test_TC03() throws Exception {
        Element parent = new Element("div");
        TextNode first = new TextNode("one");
        parent.appendChild(first);  // now childNodes non-empty
        TextNode second = new TextNode("two");
        // branch false: ensureChildNodes sees non-empty list
        parent.appendChild(second);
        // now two children
        assertEquals(2, parent.childNodeSize(), "Expected two child nodes after second appendChild");
        assertEquals(parent, second.parent(), "Expected second.child.parent() to be the parent element");
        Field sibField = TextNode.class.getSuperclass().getDeclaredField("siblingIndex");
        sibField.setAccessible(true);
        int idx = sibField.getInt(second);
        assertEquals(1, idx, "Expected second child's siblingIndex to be 1");
    }

    @Test
    @DisplayName("appendChild(existingChild) re-parents a node from old parent to new parent")
    void test_TC04() throws Exception {
        Element oldParent = new Element("span");
        Element newParent = new Element("div");
        TextNode child = new TextNode("hello");
        oldParent.appendChild(child);  // child initially under oldParent
        // now reparent child to newParent; ensure ensureChildNodes branch true for newParent
        newParent.appendChild(child);
        // oldParent should have no children after reparent
        assertEquals(0, oldParent.childNodeSize(), "Expected oldParent to have 0 children after reparenting");
        // newParent should have one child
        assertEquals(1, newParent.childNodeSize(), "Expected newParent to have 1 child after reparenting");
        // child.parent updated
        assertEquals(newParent, child.parent(), "Expected child.parent() to be newParent after reparenting");
        // siblingIndex reset to 0 under newParent
        Field sibField = TextNode.class.getSuperclass().getDeclaredField("siblingIndex");
        sibField.setAccessible(true);
        int idx = sibField.getInt(child);
        assertEquals(0, idx, "Expected reparented child's siblingIndex to be 0 in new parent");
    }
}