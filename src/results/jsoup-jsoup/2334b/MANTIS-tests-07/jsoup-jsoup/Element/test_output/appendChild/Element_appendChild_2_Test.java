package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
public class Element_appendChild_2_Test {

    @Test
    @DisplayName("TC05: appendChild reassigns a child already in same parent (remove and re-add)")
    void test_TC05() throws Exception {
        // GIVEN: an element with one child already appended
        Element el = new Element("div");
        Element child = new Element("span");
        el.appendChild(child);  // first append: child.parentNode set, child added to el.childNodes

        // WHEN: appendChild is called again with the same child
        el.appendChild(child);  // should remove and re-add the existing child per intended behavior

        // THEN: only one child remains
        assertEquals(1, el.childNodeSize(), 
            "After re-appending the same child, childNodeSize should still be 1");
        // AND: the child's parent should remain the element
        assertSame(el, child.parent(), 
            "Child.parent should be the same element after re-append");
        // AND: the child's siblingIndex should be 0 (only child)
        // siblingIndex is a private field in Node, so use reflection to verify it
        Field idxField = Node.class.getDeclaredField("siblingIndex");
        idxField.setAccessible(true);
        int idx = idxField.getInt(child);
        assertEquals(0, idx,
            "Child.siblingIndex should be reset to 0 after re-append");
    }
}