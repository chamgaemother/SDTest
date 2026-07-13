package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
public class Element_appendChild_0_Test {

    @Test
    @DisplayName("appendChild(null) throws IllegalArgumentException when child is null")
    void test_TC01() {
        Element parent = new Element("div");
        // branch B1: Validate.notNull(child) should trigger IllegalArgumentException
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> parent.appendChild(null));
        // no childNodes should have been added on exception
        assertEquals(0, parent.childNodeSize(), "childNodes should remain empty when appendChild(null) fails");
    }

    @Test
    @DisplayName("appendChild(firstChild) on empty element creates child list and sets siblingIndex 0")
    void test_TC02() throws Exception {
        Element parent = new Element("p");
        TextNode child = new TextNode("text");
        // on empty childNodes list, ensureChildNodes() is called (branch B2 true)
        Element result = parent.appendChild(child);
        // return value should be the parent element itself
        assertSame(parent, result, "appendChild should return the parent instance for chaining");
        // childNodes now has exactly one element
        assertEquals(1, parent.childNodeSize(), "childNodeSize should be 1 after first appendChild");
        // child.parent() should reference the parent
        assertSame(parent, child.parent(), "child.parent() should be set to the parent element");
        // siblingIndex is private; use reflection to verify it's 0
        Field idxField = child.getClass().getSuperclass().getDeclaredField("siblingIndex");
        idxField.setAccessible(true);
        int idx = idxField.getInt(child);
        assertEquals(0, idx, "siblingIndex should be 0 for the first child");
    }

    @Test
    @DisplayName("appendChild(secondChild) on non-empty element skips ensureChildNodes and sets siblingIndex 1")
    void test_TC03() throws Exception {
        Element parent = new Element("p");
        // first child to populate childNodes; ensureChildNodes() already done, future calls skip creation
        TextNode first = new TextNode("one");
        parent.appendChild(first);
        TextNode second = new TextNode("two");
        // path B3→B4: ensureChildNodes is skipped as childNodes != EmptyNodes
        parent.appendChild(second);
        assertEquals(2, parent.childNodeSize(), "childNodeSize should be 2 after appending second child");
        assertSame(parent, second.parent(), "second.parent() should be set to the parent element");
        Field idxField = second.getClass().getSuperclass().getDeclaredField("siblingIndex");
        idxField.setAccessible(true);
        int idx = idxField.getInt(second);
        assertEquals(1, idx, "siblingIndex should be 1 for the second child");
    }

    @Test
    @DisplayName("appendChild(movingChild) reassigns from old parent to new parent")
    void test_TC04() {
        Element oldParent = new Element("ul");
        Element newParent = new Element("div");
        TextNode child = new TextNode("item");
        // initially attach to oldParent
        oldParent.appendChild(child);
        assertEquals(1, oldParent.childNodeSize(), "precondition: oldParent should have 1 child");
        // calling appendChild moves the node from oldParent to newParent
        newParent.appendChild(child);
        // oldParent.childNodes should be emptied (branch B2 true for newParent, B3->B5 true)
        assertEquals(0, oldParent.childNodeSize(), "oldParent should have 0 children after moving");
        assertEquals(1, newParent.childNodeSize(), "newParent should have 1 child after moving");
        assertSame(newParent, child.parent(), "child.parent() should now reference newParent");
    }
}