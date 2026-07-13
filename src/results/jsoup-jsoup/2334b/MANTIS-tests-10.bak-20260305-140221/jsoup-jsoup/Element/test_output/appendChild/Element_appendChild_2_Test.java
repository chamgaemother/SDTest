package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Element_appendChild_2_Test {

    @Test
    @DisplayName("TC05: Reparent first of two children updates remaining siblingIndex in original parent (reparentChild true & removeChild sibling-reindex branch)")
    void test_TC05() {
        // GIVEN: parent1 has two child elements A and B
        Element parent1 = new Element("div");
        Element parent2 = new Element("section");
        Element A = new Element("span");
        Element B = new Element("em");
        parent1.appendChild(A); // first child, siblingIndex=0
        parent1.appendChild(B); // second child, siblingIndex=1
        assertEquals(2, parent1.childrenSize(), "precondition: parent1 should have two children");

        // WHEN: reparent the first child A into parent2
        Element result = parent2.appendChild(A);
        // The branch reparentChild==true triggers removal of A from parent1 (removeChild) and reindex of remaining siblings

        // THEN: original parent1 should have only one child (B) with index updated to 0
        assertEquals(1, parent1.childrenSize(), "parent1 should now have one child after reparent");
        assertEquals(0, B.siblingIndex(), "remaining child B should have siblingIndex 0 after reparent and reindex");

        // AND: new parent2 should have A as its only child with index 0
        assertEquals(1, parent2.childrenSize(), "parent2 should have one child after appendChild");
        assertEquals(0, A.siblingIndex(), "reparented child A should have siblingIndex 0 in new parent");

        // AND: appendChild returns the receiving parent for chaining
        assertSame(parent2, result, "appendChild should return the receiving parent (parent2)");
    }

    @Test
    @DisplayName("TC06: appendChild returns this allowing chaining multiple adds on fresh element (return-this branch)")
    void test_TC06() {
        // GIVEN: a fresh parent with no children
        Element parent = new Element("ul");
        TextNode C1 = new TextNode("one");
        TextNode C2 = new TextNode("two");

        // WHEN: chain two appendChild calls
        // The first appendChild sees reparentChild false (child had no parent), goes through ensureChildNodes->add->setSiblingIndex.
        // The second appendChild reuses the same target, demonstrating the return-this branch for chaining.
        Element chained = parent.appendChild(C1).appendChild(C2);

        // THEN: the returned object should be the same parent for chaining
        assertSame(parent, chained, "appendChild chaining should return same parent instance");

        // AND: both children are added in order with correct siblingIndex values
        assertEquals(2, parent.childNodeSize(), "parent should have two child nodes after chaining");
        assertEquals(0, C1.siblingIndex(), "first appended child C1 should have siblingIndex 0");
        assertEquals(1, C2.siblingIndex(), "second appended child C2 should have siblingIndex 1");
    }
}