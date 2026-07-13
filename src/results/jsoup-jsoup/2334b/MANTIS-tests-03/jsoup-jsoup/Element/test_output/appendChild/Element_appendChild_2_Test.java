package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class Element_appendChild_2_Test {

    @Test
    @DisplayName("appendChild detaches a mid-list child and updates sibling indices of remaining and moved child")
    public void test_TC07() {
        // GIVEN: oldParent has two children c1 and c2
        Element oldParent = new Element("ul");
        TextNode c1 = new TextNode("one");
        TextNode c2 = new TextNode("two");
        oldParent.appendChild(c1);
        oldParent.appendChild(c2);
        Element newParent = new Element("div");
        // Precondition check: c1 at index 0, c2 at index 1
        assertEquals(2, oldParent.childNodeSize(), "Precondition: two children before reparenting");
        assertEquals(0, c1.siblingIndex(), "c1 should start at index 0");
        assertEquals(1, c2.siblingIndex(), "c2 should start at index 1");

        // WHEN: reparent the first child c1 to newParent
        // This exercises the 'reparentChild' logic before adding to new list (path B0→B2→B3)
        newParent.appendChild(c1);

        // THEN: oldParent should now have only c2 at index 0 and siblingIndex updated
        assertEquals(1, oldParent.childNodeSize(), "oldParent should have one child after c1 is moved");
        assertEquals(c2, oldParent.child(0), "Remaining child in oldParent should be c2");
        assertEquals(0, c2.siblingIndex(), "c2's siblingIndex should update to 0 after removal of c1");

        // THEN: newParent should have c1 at index 0 and its siblingIndex set to 0
        assertEquals(1, newParent.childNodeSize(), "newParent should have one child after append");
        assertEquals(c1, newParent.child(0), "Child of newParent should be c1");
        assertEquals(0, c1.siblingIndex(), "c1's siblingIndex should be 0 in newParent");
    }

    @Test
    @DisplayName("appendChild uses existing NodeList when ensureChildNodes was called prior")
    public void test_TC08() {
        // GIVEN: parent.ensureChildNodes() called to initialize internal list (avoid EmptyNodes path)
        Element parent = new Element("div");
        // ensureChildNodes should replace the EmptyNodes placeholder with a real NodeList
        parent.ensureChildNodes(); // this makes childNodes != EmptyNodes
        TextNode child = new TextNode("text");

        // Precondition: no children present
        assertEquals(0, parent.childNodeSize(), "Precondition: no children before append");

        // WHEN: appendChild should add to the existing NodeList branch (path B0→B2(false)→B3)
        parent.appendChild(child);

        // THEN: child appended normally at index 0 and siblingIndex set correctly
        assertEquals(1, parent.childNodeSize(), "parent should have one child after append");
        assertEquals(child, parent.child(0), "First child should be the appended node");
        assertEquals(0, child.siblingIndex(), "Appended child's siblingIndex should be 0");
    }
}