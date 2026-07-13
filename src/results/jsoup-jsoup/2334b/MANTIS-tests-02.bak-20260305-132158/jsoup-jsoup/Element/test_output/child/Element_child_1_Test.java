package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.jsoup.nodes.DataNode;

/**
 * Test class for org.jsoup.nodes.Element.child(int) method.
 * Scenarios:
 * TC07: child(0) on element with only non-Element children throws IndexOutOfBoundsException
 * TC08: second call to child(1) uses cached shadowChildrenRef path, returns correct Element
 */
public class Element_child_1_Test {

    @Test
    @DisplayName("TC07: child(0) on element with only non-Element children throws IndexOutOfBoundsException")
    public void test_TC07() {
        // GIVEN an element with only TextNode and DataNode children (no Element children)
        Element parent = new Element("div");
        // two text nodes
        parent.appendText("text1"); // adds TextNode
        parent.appendText("text2"); // adds TextNode
        // one DataNode child directly, not wrapped in Element
        parent.appendChild(new DataNode("data")); // adds DataNode
        // Sanity: childElementsList() should be empty, so child(0) should throw
        assertThrows(IndexOutOfBoundsException.class, () -> {
            // WHEN calling child(0)
            parent.child(0);
        }, "Expected IndexOutOfBoundsException when no Element children exist");
    }

    @Test
    @DisplayName("TC08: second call to child(1) uses cached shadowChildrenRef path, returns correct Element")
    public void test_TC08() {
        // GIVEN a parent with two direct Element children
        Element parent = new Element("ul");
        Element first = parent.appendElement("li");  // first Element child
        Element second = parent.appendElement("li"); // second Element child
        // At this point, childElementsList() computes and caches a list of [first, second]

        // WHEN first call to child(1)
        Element result1 = parent.child(1);
        // THEN it should return the second element
        assertSame(second, result1, "First call to child(1) should return the second element");

        // WHEN second call to child(1), shadowChildrenRef is reused, bypassing list recomputation
        Element result2 = parent.child(1);
        // THEN it still returns the same second element instance
        assertSame(second, result2, "Second call to child(1) should also return the cached second element");
    }
}