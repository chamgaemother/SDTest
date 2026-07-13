package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class Element_child_1_Test {

    @Test
    @DisplayName("child(index) rebuilds cached childElementsList after mutation via appendChild and returns new element")
    void test_TC11() {
        // GIVEN: a parent with two children to build the initial cache (loop×2 → cache store)
        Element parent = new Element("div");
        Element a = new Element("span");
        Element b = new Element("span");
        parent.appendChild(a);
        parent.appendChild(b);
        // First call to child builds the shadowChildrenRef cache
        Element firstCall = parent.child(1);
        assertEquals(b, firstCall, "Initial child(1) should return the second child 'b'");

        // MUTATE: append a new third child (invalidates cache, ensures path B0 mutated then B3 with loop×3)
        Element c = new Element("span");
        parent.appendChild(c);

        // WHEN: retrieving child(2) after mutation should rebuild cache and return the new child
        Element result = parent.child(2);

        // THEN: the returned element is the newly appended third element 'c'
        assertEquals(c, result, "After cache invalidation, child(2) should return the newly appended third child 'c'");
    }
}