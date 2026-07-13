package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import static org.junit.jupiter.api.Assertions.*;
public class Element_child_2_Test {

    @Test
    @DisplayName("TC09: After caching childElementsList, empty() invalidates cache and child(0) throws IndexOutOfBoundsException")
    public void test_TC09() {
        // GIVEN: a parent with one child element; first call to child(0) populates the shadowChildrenRef cache
        Element parent = new Element("div");
        Element child = parent.appendElement("p");
        Element firstCall = parent.child(0);
        assertSame(child, firstCall, "First child() should return the only child and populate cache");
        // WHEN: we empty the parent, which should clear childNodes and call nodelistChanged to invalidate cache
        parent.empty();
        // THEN: subsequent child(0) should re-compute childElementsList (now empty) and throw IndexOutOfBoundsException
        assertThrows(IndexOutOfBoundsException.class, () -> {
            parent.child(0);
        }, "After empty(), child(0) on an empty element should throw IndexOutOfBoundsException");
    }

    @Test
    @DisplayName("TC10: child(1) filters only Element nodes among mixed children and returns second Element")
    public void test_TC10() {
        // GIVEN: container with mixed node types: TextNode, Element a, TextNode, Element b
        Element container = new Element("div");
        container.appendText("t1"); // text node 0
        Element a = container.appendElement("a"); // element 0 in filtered list
        container.appendText("t2"); // text node 1
        Element b = container.appendElement("b"); // element 1 in filtered list
        // WHEN: calling child(1) should skip TextNodes and return the second Element 'b'
        Element result = container.child(1);
        // THEN: result must be exactly the second element 'b'
        assertSame(b, result, "child(1) should filter out text nodes and return the second Element child");
    }

}