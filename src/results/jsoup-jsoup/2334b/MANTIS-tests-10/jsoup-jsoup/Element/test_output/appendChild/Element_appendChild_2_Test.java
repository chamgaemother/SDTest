package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for org.jsoup.nodes.Element.appendChild
 */
public class Element_appendChild_2_Test {

    @Test
    @DisplayName("appendChild(reparentedChild) from parentA with two existing children to parentB with two existing children sets correct siblingIndex on new parent")
    public void test_TC12() {
        // GIVEN: two parent elements each with two children; c1 initially at index 0 under parentA
        Element parentA = new Element("div");
        Element parentB = new Element("div");
        Element c1 = new Element("p");
        Element c2 = new Element("p");
        TextNode b1 = new TextNode("x");
        TextNode b2 = new TextNode("y");
        // Build initial trees
        parentA.appendChild(c1); // B0: childNodes was EmptyNodes, now non-empty
        parentA.appendChild(c2); // ensures c1 has a parent and siblingIndex 0, c2 at 1
        parentB.appendChild(b1); // parentB has one text child
        parentB.appendChild(b2); // parentB now has two text children

        // Sanity: precondition check
        assertEquals(2, parentA.childNodeSize(), "parentA should start with 2 children");
        assertEquals(2, parentB.childNodeSize(), "parentB should start with 2 children");
        assertEquals(0, c1.siblingIndex(), "c1 should start with siblingIndex 0");

        // WHEN: reparent c1 to parentB
        Element returned = parentB.appendChild(c1);
        // B2(true): child had existing parent, so reparentChild branch taken

        // THEN: parentA lost one child, only c2 remains
        assertEquals(1, parentA.childNodeSize(), "c1 must be removed from original parentA");
        // parentB now has three children b1, b2, and c1 appended at end
        assertEquals(3, parentB.childNodeSize(), "parentB should have 3 children after append");
        // c1 parent reassigned to parentB
        assertSame(parentB, c1.parent(), "c1.parent() should be parentB after reparenting");
        // c1 siblingIndex should now be 2 (end of list)
        assertEquals(2, c1.siblingIndex(), "c1 should be at index 2 under new parentB");
        // method should return the parentB for chaining
        assertSame(parentB, returned, "appendChild should return the parentB instance for chaining");
    }
}