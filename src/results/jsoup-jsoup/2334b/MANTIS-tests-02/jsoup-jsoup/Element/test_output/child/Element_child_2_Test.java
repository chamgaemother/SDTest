package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for Element.child(int) method, covering both empty and mixed child scenarios.
 */
public class Element_child_2_Test {

    @Test
    @DisplayName("second invocation on element with only non-Element children uses cached empty list and still throws IndexOutOfBoundsException")
    public void test_TC09() {
        // GIVEN: a parent with only TextNode children (no Element children -> empty filtered list)
        Element parent = new Element("div");
        parent.appendChild(new TextNode("foo"));
        
        // WHEN / THEN: first call to child(0) should throw IndexOutOfBoundsException because no Element children
        assertThrows(IndexOutOfBoundsException.class, () -> {
            parent.child(0);
        }, "Expected IndexOutOfBoundsException when accessing child(0) on empty element list");
        
        // WHEN / THEN: second call still throws, using the cached empty shadowChildrenRef list
        assertThrows(IndexOutOfBoundsException.class, () -> {
            parent.child(0);
        }, "Expected IndexOutOfBoundsException on second access due to cached empty children list");
    }

    @Test
    @DisplayName("child(1) returns second Element in mixed children with interleaved non-Element nodes")
    public void test_TC10() {
        // GIVEN: a parent with interleaved TextNode and Element children
        Element parent = new Element("div");
        parent.appendChild(new TextNode("t1"));           // non-Element node
        Element el1 = new Element("span");                 // first Element
        parent.appendChild(el1);
        parent.appendChild(new TextNode("t2"));           // another non-Element node
        Element el2 = new Element("a");                    // second Element
        parent.appendChild(el2);
        
        // WHEN: retrieving child(1) should skip non-Element nodes and return the second Element (el2)
        Element result = parent.child(1);
        
        // THEN: assert exact same instance is returned
        assertSame(el2, result, "Expected child(1) to return the second Element (el2) in filtered children list");
    }
}