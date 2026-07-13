package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class Element_child_0_Test {

    @Test
    @DisplayName("child(0) on element with no children throws IndexOutOfBoundsException (childNodeSize==0)")
    void test_TC01() {
        // GIVEN an empty parent element, so childNodeSize()==0 branch is taken immediately
        Element parent = new Element("div");
        // WHEN & THEN expect IndexOutOfBoundsException since there are no child elements
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(0));
    }

    @Test
    @DisplayName("child(0) on element with one TextNode child throws IndexOutOfBoundsException (loop finds zero Element children)")
    void test_TC02() {
        // GIVEN a parent with one TextNode child: childNodeSize()>0 true, but no Element in loop
        Element parent = new Element("div");
        parent.appendChild(new TextNode("text"));
        // WHEN & THEN expect IndexOutOfBoundsException because filtered Element list is empty
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(0));
    }

    @Test
    @DisplayName("child(0) returns only Element child when exactly one Element is present")
    void test_TC03() {
        // GIVEN a parent with exactly one Element child: childNodeSize()>0 and one loop hit
        Element parent = new Element("div");
        Element child = new Element("span");
        parent.appendChild(child);
        // WHEN retrieving child(0)
        Element result = parent.child(0);
        // THEN should return the exact same instance
        assertSame(child, result);
    }

    @Test
    @DisplayName("child(1) on mixed children returns second Element, skipping TextNode")
    void test_TC04() {
        // GIVEN mixed children: first Element, then TextNode, then second Element
        Element parent = new Element("div");
        Element first = new Element("a");
        parent.appendChild(first);
        parent.appendChild(new TextNode("x"));
        Element second = new Element("b");
        parent.appendChild(second);
        // WHEN retrieving child(1): should skip non-Element and pick the second Element
        Element result = parent.child(1);
        // THEN result is the "b" element
        assertSame(second, result);
    }

    @Test
    @DisplayName("child(-1) with negative index throws IndexOutOfBoundsException (invalid index)")
    void test_TC05() {
        // GIVEN one Element child: childNodeSize()>0, loop would find one element,
        // but index -1 is invalid
        Element parent = new Element("div");
        parent.appendChild(new Element("p"));
        // WHEN & THEN negative index should produce IndexOutOfBoundsException
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(-1));
    }

    @Test
    @DisplayName("child(2) where index equals number of elements throws IndexOutOfBoundsException (off-by-one)")
    void test_TC06() {
        // GIVEN two Element children: childNodeSize()>0, loop yields two elements at indices 0 and 1,
        // but requesting index 2 equals size and is out of bounds
        Element parent = new Element("div");
        parent.appendChild(new Element("p"));
        parent.appendChild(new Element("q"));
        // WHEN & THEN index==size should throw IndexOutOfBoundsException
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(2));
    }
}