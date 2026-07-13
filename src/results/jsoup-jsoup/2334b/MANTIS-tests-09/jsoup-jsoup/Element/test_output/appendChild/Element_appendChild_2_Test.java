package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
public class Element_appendChild_2_Test {

    @Test
    @DisplayName("TC07: appendChild(DataNode) on empty childNodes creates NodeList and sets sibling index")
    public void test_TC07() {
        // GIVEN a fresh Element with no children
        Element parent = new Element("div");
        DataNode data = new DataNode("hello");

        // WHEN appendChild is called (invokes reparentChild -> ensureChildNodes branch)
        parent.appendChild(data);

        // THEN exactly one child was added
        assertEquals(1, parent.childNodeSize(), "childNodeSize should be 1 after first appendChild");
        // AND the DataNode's parent field must point to our parent element
        assertSame(parent, data.parent(), "DataNode.parent should be reset to the Element on appendChild");
        // AND the siblingIndex for the only child must be zero
        assertEquals(0, data.siblingIndex(), "siblingIndex of the only child should be 0");
    }

    @Test
    @DisplayName("TC08: appendChild on Element whose childNodes is already a NodeList (skip ensureChildNodes branch)")
    public void test_TC08() throws Exception {
        // GIVEN an Element whose childNodes has already been initialized via ensureChildNodes
        Element parent = new Element("div");
        // force the ensureChildNodes = false branch on subsequent appends
        Method ensure = Element.class.getDeclaredMethod("ensureChildNodes");
        ensure.setAccessible(true);
        // calling first to switch childNodes from the empty sentinel to a NodeList
        @SuppressWarnings("unused")
        Object forced = ensure.invoke(parent);

        // first append to create index 0
        Element child1 = new Element("span");
        parent.appendChild(child1);

        // WHEN appendChild is called again, ensureChildNodes() is skipped this time
        Element child2 = new Element("p");
        parent.appendChild(child2);

        // THEN we have two children total
        assertEquals(2, parent.childNodeSize(), "childNodeSize should be 2 after two appends");
        // AND the new child's siblingIndex must reflect its position at index 1
        assertEquals(1, child2.siblingIndex(), "siblingIndex of the second child should be 1");
    }

    @Test
    @DisplayName("TC09: appendChild(same DataNode twice) triggers reparent removal and re-add at end")
    public void test_TC09() {
        // GIVEN an Element and a DataNode already appended once
        Element parent = new Element("div");
        DataNode data = new DataNode("x");
        parent.appendChild(data);
        assertEquals(1, parent.childNodeSize(), "precondition: childNodeSize must be 1 after initial append");

        // WHEN appendChild is called again with the same DataNode (should remove old then re-add at end)
        parent.appendChild(data);

        // THEN still exactly one child in the list
        assertEquals(1, parent.childNodeSize(), "childNodeSize should remain 1 after re-appending same node");
        // AND the DataNode's siblingIndex must reset to 0 as it's the only element again
        assertEquals(0, data.siblingIndex(), "siblingIndex should be reset to 0 after re-adding the same node");
    }
}