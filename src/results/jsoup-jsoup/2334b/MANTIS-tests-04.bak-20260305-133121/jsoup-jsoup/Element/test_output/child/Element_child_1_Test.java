package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
public class Element_child_1_Test {

    @Test
    @DisplayName("child(2) on element with two children throws IndexOutOfBoundsException for index > max")
    public void test_TC06() {
        // GIVEN: an element with exactly two children
        Element parent = new Element("div");
        parent.appendElement("a"); // first child
        parent.appendElement("b"); // second child
        int index = 2; // BOUNDARY: index equal to number of children

        // WHEN & THEN: calling child(2) should throw IndexOutOfBoundsException
        assertThrows(IndexOutOfBoundsException.class, () -> {
            parent.child(index);
        });
    }

    @Test
    @DisplayName("child(1) rebuilds cache after mutation (shadowChildrenRef invalidated branch)")
    public void test_TC07() throws Exception {
        // GIVEN: parent has one child, cache is built by invoking child(0)
        Element parent = new Element("ul");
        Element first = parent.appendElement("li"); // original first child
        Element initial = parent.child(0); // build shadowChildrenRef cache (B3→B4)
        // verify initial cache by identity
        assertEquals(first, initial);

        // WHEN: prepend a new element (mutation) to invalidate cache
        Element second = new Element("li");
        parent.prependChild(second); // triggers nodelistChanged -> cache invalidation
        // THEN: child(1) should rebuild cache and return the original first element
        Element result = parent.child(1); // rebuilds cache and retrieves 2nd element (original first)
        assertEquals(first, result);
    }
}