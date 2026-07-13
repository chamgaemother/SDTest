package org.jsoup.nodes;

import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
public class Element_child_0_Test {

    @Test
    @DisplayName("child(0) on element with zero element children throws IndexOutOfBoundsException")
    void test_TC01() {
        // GIVEN parent element with no children -> childNodes is empty, so filtering yields empty list
        Element parent = new Element("div");
        // WHEN parent.child(0) is invoked
        // THEN expect IndexOutOfBoundsException
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(0));
    }

    @Test
    @DisplayName("child(-1) on element with one element child throws IndexOutOfBoundsException")
    void test_TC02() {
        // GIVEN parent with one element child -> one element present, but index -1 is negative
        Element parent = new Element("div");
        Element child1 = new Element("span");
        parent.appendChild(child1);
        // WHEN parent.child(-1) is invoked
        // THEN expect IndexOutOfBoundsException for negative index
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(-1));
    }

    @Test
    @DisplayName("child(1) on element with one element child throws IndexOutOfBoundsException")
    void test_TC03() {
        // GIVEN parent with one element child -> filtered list size is 1, index equal to size is out of bounds
        Element parent = new Element("div");
        Element child1 = new Element("span");
        parent.appendChild(child1);
        // WHEN parent.child(1) is invoked
        // THEN expect IndexOutOfBoundsException for index == size
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(1));
    }

    @Test
    @DisplayName("child(0) on element with exactly one element child returns that child")
    void test_TC04() {
        // GIVEN parent with one element child child1 -> B3 loop iterates once finding the element
        Element parent = new Element("div");
        Element child1 = new Element("span");
        parent.appendChild(child1);
        // WHEN Element result = parent.child(0)
        Element result = parent.child(0);
        // THEN assert result == child1
        assertEquals(child1, result);
    }

    @Test
    @DisplayName("child(1) on element with mixed childNodes returns second Element after filtering")
    void test_TC05() {
        // GIVEN parent with TextNode, p, a appended -> filtering yields two elements [e1, e2]
        Element parent = new Element("div");
        parent.appendChild(new TextNode("txt")); // non-element, should be skipped
        Element e1 = new Element("p");
        Element e2 = new Element("a");
        parent.appendChild(e1);
        parent.appendChild(e2);
        // WHEN Element result = parent.child(1)
        Element result = parent.child(1);
        // THEN assert result == e2
        assertEquals(e2, result);
    }

    @Test
    @DisplayName("child(0) on element with non-element children throws IndexOutOfBoundsException after filtering")
    void test_TC06() {
        // GIVEN parent with only TextNode and DataNode -> filtering yields empty element list
        Element parent = new Element("div");
        parent.appendChild(new TextNode("one"));
        parent.appendChild(new DataNode("two"));
        // WHEN parent.child(0) is invoked
        // THEN expect IndexOutOfBoundsException because no elements after filter
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(0));
    }
}