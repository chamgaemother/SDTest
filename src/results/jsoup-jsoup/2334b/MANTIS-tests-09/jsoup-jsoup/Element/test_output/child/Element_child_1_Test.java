package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for Element.child(int) method.
 */
public class Element_child_1_Test {

    @Test
    @DisplayName("TC08: child(0) on element whose childNodes contain only non-Element types should throw IndexOutOfBoundsException (filtered list empty branch)")
    public void test_TC08() {
        // GIVEN: an Element with only non-Element child nodes (TextNode and Comment)
        Element parent = new Element("div");
        // Append a TextNode and a Comment -- ensures childElementsList() returns an empty list (no Element instances)
        parent.appendChild(new TextNode("foo")); // TextNode is not an Element
        parent.appendChild(new Comment("bar"));   // Comment is not an Element

        // WHEN / THEN: calling child(0) should attempt to access the 0th element in the filtered list (empty) and throw
        assertThrows(IndexOutOfBoundsException.class,
            () -> parent.child(0),
            "Expected IndexOutOfBoundsException when no Element children exist"
        );
    }
}