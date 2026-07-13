package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Element_appendChild_0_Test {

    @Test
    @DisplayName("Calling appendChild(null) should throw IllegalArgumentException for null child parameter (Validate.notNull)")
    public void test_TC01() {
        Element parent = new Element("div");
        Node child = null;
        // Passing null should hit Validate.notNull and throw IllegalArgumentException before any state change
        assertThrows(IllegalArgumentException.class, () -> parent.appendChild(child));
        // parent should remain with no child nodes
        assertEquals(0, parent.childNodeSize(), "Parent must not gain children when null is passed");
    }

    @Test
    @DisplayName("appendChild on empty parent creates childNodes list and adds the child with siblingIndex 0")
    public void test_TC02() {
        Element parent = new Element("div");
        TextNode child = new TextNode("text");
        // parent.childNodes is initially the shared EmptyNodes, so ensureChildNodes path (branch-true) is exercised
        Element result = parent.appendChild(child);
        // should return this parent
        assertSame(parent, result, "appendChild must return the parent instance for chaining");
        // now the childNodes must have size 1
        assertEquals(1, parent.childNodeSize(), "After first append, childNodeSize should be 1");
        // child.parent must be set to the parent
        assertSame(parent, child.parent(), "Child parent must be the element it was appended to");
        // first child gets siblingIndex 0
        assertEquals(0, child.siblingIndex(), "First child should have siblingIndex 0");
    }

    @Test
    @DisplayName("appendChild on parent with existing children skips list creation and adds second child with siblingIndex 1")
    public void test_TC03() {
        Element parent = new Element("span");
        TextNode first = new TextNode("one");
        parent.appendChild(first);
        // childNodes now is non-empty, so ensureChildNodes path (branch-false) is exercised
        TextNode second = new TextNode("two");
        Element result = parent.appendChild(second);
        // should return the same parent
        assertSame(parent, result);
        // should now have 2 children
        assertEquals(2, parent.childNodeSize(), "After appending second child, size should be 2");
        // second child's parent set correctly
        assertSame(parent, second.parent());
        // siblingIndex should reflect second position
        assertEquals(1, second.siblingIndex(), "Second child should have siblingIndex 1");
    }

    @Test
    @DisplayName("appendChild moves a node from its old parent to new parent and sets correct siblingIndex")
    public void test_TC04() {
        Element oldParent = new Element("p");
        TextNode child = new TextNode("c");
        oldParent.appendChild(child);
        // oldParent now has one child; will be removed when reparenting
        Element newParent = new Element("section");
        // reparent existing child: remove from oldParent and add to newParent
        newParent.appendChild(child);
        // oldParent should have emptied its child list
        assertEquals(0, oldParent.childNodeSize(), "Old parent should have no children after reparenting");
        // newParent should have exactly one child now
        assertEquals(1, newParent.childNodeSize(), "New parent should have one child after appending the moved node");
        // child's parent must be newParent
        assertSame(newParent, child.parent(), "Child parent should be updated to new parent");
        // child should have siblingIndex 0 as first in newParent
        assertEquals(0, child.siblingIndex(), "Moved child should have siblingIndex 0 in the new parent");
    }
}