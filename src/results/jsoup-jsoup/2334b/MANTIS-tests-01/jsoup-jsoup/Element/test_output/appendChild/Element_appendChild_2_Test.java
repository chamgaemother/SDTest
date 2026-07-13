package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JUnit tests for Element.appendChild method scenarios.
 */
public class Element_appendChild_2_Test {

    @Test
    @DisplayName("appendChild reparenting a middle child removes it and shifts remaining siblings' indexes")
    public void test_TC07() {
        // GIVEN: oldParent with three children A, B, C; newParent empty
        Element oldParent = new Element("div");
        Element newParent = new Element("div");
        Element A = new Element("p");
        Element B = new Element("span");
        Element C = new Element("a");
        oldParent.appendChild(A);
        oldParent.appendChild(B);
        oldParent.appendChild(C);
        // Precondition ensures B is at index 1; oldParent.childNodeSize()==3
        assertEquals(3, oldParent.childNodeSize());

        // WHEN: reparent middle child B to newParent
        newParent.appendChild(B);
        // This should trigger reparent logic: remove B from oldParent and add to newParent

        // THEN: oldParent retains A and C at new indexes 0 and 1
        assertEquals(2, oldParent.childNodeSize(), "oldParent should have 2 children after reparenting B");
        assertEquals(A, oldParent.child(0), "First child should remain A");
        assertEquals(0, A.siblingIndex(), "A siblingIndex should update to 0 after reparenting B");
        assertEquals(C, oldParent.child(1), "Second child should now be C after B removal");
        assertEquals(1, C.siblingIndex(), "C siblingIndex should update to 1 after B removal");

        // AND newParent has B at index 0
        assertEquals(1, newParent.childNodeSize(), "newParent should have exactly one child after appendChild(B)");
        assertEquals(B, newParent.child(0), "newParent's child should be B");
        assertEquals(0, B.siblingIndex(), "B siblingIndex in newParent should be 0");
    }

    @Test
    @DisplayName("appendChild on same parent moves existing child from start to end and updates indexes")
    public void test_TC08() {
        // GIVEN: parent with two children X, Y added in order
        Element parent = new Element("ul");
        Element X = new Element("li");
        Element Y = new Element("li");
        parent.appendChild(X);
        parent.appendChild(Y);
        // Precondition: X at index 0, Y at index 1
        assertEquals(2, parent.childNodeSize());
        assertEquals(X, parent.child(0));
        assertEquals(0, X.siblingIndex());
        assertEquals(Y, parent.child(1));
        assertEquals(1, Y.siblingIndex());

        // WHEN: appendChild(X) on same parent should move X from index 0 to end (index 1)
        parent.appendChild(X);
        // This invokes reparentChild on same parent, then add to end

        // THEN: order becomes [Y, X] and indexes update accordingly
        assertEquals(2, parent.childNodeSize(), "parent should still have 2 children after re-appendSelf");
        assertEquals(Y, parent.child(0), "First child should now be Y after moving X to end");
        assertEquals(0, Y.siblingIndex(), "Y siblingIndex should update to 0 after X moved");
        assertEquals(X, parent.child(1), "Second child should now be X after reappending");
        assertEquals(1, X.siblingIndex(), "X siblingIndex should update to 1 after moving to end");
    }
}