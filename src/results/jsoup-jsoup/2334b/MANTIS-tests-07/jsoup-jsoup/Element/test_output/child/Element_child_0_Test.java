package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for org.jsoup.nodes.Element#child(int)
 */
public class Element_child_0_Test {

    @Test
    @DisplayName("child(0) on element with no children throws IndexOutOfBoundsException (childNodeSize()==0 branch)")
    public void test_TC01() {
        // GIVEN an element with no children: childNodeSize()==0 triggers immediate empty filter
        Element parent = new Element("div");
        // WHEN/THEN expecting IndexOutOfBoundsException because there are no element children
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(0));
    }

    @Test
    @DisplayName("child(0) on element with one non-Element child throws IndexOutOfBoundsException (filtered out, loop with N=1)")
    public void test_TC02() {
        // GIVEN an element with a single TextNode child: childNodes.size()>0 but no Element instances
        Element parent = new Element("div");
        parent.appendChild(new TextNode("text")); // not an Element, so filtered out
        // WHEN/THEN expecting IndexOutOfBoundsException because filtered element count is zero
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(0));
    }

    @Test
    @DisplayName("child(0) returns sole Element child when exactly one Element present")
    public void test_TC03() {
        // GIVEN an element with exactly one Element child
        Element parent = new Element("div");
        Element child = new Element("span");
        parent.appendChild(child); // loop will see one element
        // WHEN retrieving child at index 0
        Element result = parent.child(0);
        // THEN should return the same instance
        assertSame(child, result);
    }

    @Test
    @DisplayName("child(1) on element with two Element children returns second child (loop N=2, index in range)")
    public void test_TC04() {
        // GIVEN an element with two Element children
        Element parent = new Element("div");
        Element e1 = new Element("p");
        Element e2 = new Element("a");
        parent.appendChild(e1);
        parent.appendChild(e2);
        // WHEN retrieving child at index 1
        Element result = parent.child(1);
        // THEN should return the second element instance
        assertSame(e2, result);
    }

    @Test
    @DisplayName("child(1) on element with one Element child throws IndexOutOfBoundsException (index >= size)")
    public void test_TC05() {
        // GIVEN an element with one Element child
        Element parent = new Element("div");
        Element child = new Element("span");
        parent.appendChild(child); // only one element in loop
        // WHEN retrieving child at index 1 (out of range)
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(1));
    }

    @Test
    @DisplayName("child(-1) on element with one Element child throws IndexOutOfBoundsException (negative index)")
    public void test_TC06() {
        // GIVEN an element with one Element child
        Element parent = new Element("div");
        Element child = new Element("span");
        parent.appendChild(child);
        // WHEN retrieving child at negative index
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(-1));
    }

    @Test
    @DisplayName("child(0) on element with mixed children returns only Element child at correct filtered index")
    public void test_TC07() {
        // GIVEN an element with mixed Node types: TextNode, Element, DataNode
        Element parent = new Element("div");
        parent.appendChild(new TextNode("x"));                // TextNode should be skipped
        Element elm = new Element("span");
        parent.appendChild(elm);                                // First Element at filtered index 0
        parent.appendChild(new DataNode("d"));               // DataNode should be skipped
        // WHEN retrieving child at index 0 among Elements
        Element result = parent.child(0);
        // THEN should return the span element
        assertSame(elm, result);
    }
}