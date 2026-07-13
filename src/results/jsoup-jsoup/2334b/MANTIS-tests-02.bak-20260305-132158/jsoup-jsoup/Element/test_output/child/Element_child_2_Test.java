package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.DataNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for Element.child(int) method covering cache invalidation and filtering logic.
 */
public class Element_child_2_Test {

    @Test
    @DisplayName("After initial child(0) caches shadowChildrenRef, appending a new element invalidates cache and child(1) returns the new element")
    public void test_TC09() {
        // GIVEN: parent element with one <li> child, calling child(0) builds shadowChildrenRef cache (path B0→B1)
        Element parent = new Element("ul");
        Element first = parent.appendElement("li");
        // Force cache creation by retrieving the first element
        assertSame(first, parent.child(0), "Initial child(0) should return the first appended element and build cache");

        // WHEN: append a second <li>, which triggers nodelistChanged and invalidates shadowChildrenRef (path B0→B1 after cache-null)
        Element second = parent.appendElement("li");
        // THEN: child(1) should rebuild cache and return the newly appended second element (loop ×2 then B2)
        Element result = parent.child(1);
        assertSame(second, result, "After cache invalidation, child(1) must return the newly appended second element");
    }

    @Test
    @DisplayName("child(1) on element with interleaved TextNode, DataNode, Element, TextNode, Element returns the second Element")
    public void test_TC10() {
        // GIVEN: parent with mixed children: TextNode, DataNode, Element first, TextNode, Element second
        Element parent = new Element("div");
        parent.appendText("a");                        // TextNode: should be skipped by childElementsList filter
        parent.appendChild(new DataNode("d"));        // DataNode: should be skipped as well
        Element first = parent.appendElement("span"); // first Element in the filtered list
        parent.appendText("b");                        // another TextNode to skip
        Element second = parent.appendElement("span"); // second Element in the filtered list

        // WHEN: retrieve the element-at-index 1 from childElementsList (only Elements count)
        Element result = parent.child(1);

        // THEN: must return the second Element, skipping non-Element nodes (path B0→B1 filter non-Element → B2)
        assertSame(second, result, "child(1) should skip TextNode and DataNode, returning the second Element only");
    }
}