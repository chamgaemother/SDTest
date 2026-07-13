package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class Element_child_0_Test {

    @Test
    @DisplayName("TC01: call child(0) on element with no child nodes throws IndexOutOfBoundsException")
    public void test_TC01() {
        // GIVEN an element with no children (childNodeSize() == 0) to trigger the zero-size branch
        Element parent = new Element("div");
        // WHEN / THEN calling child(0) must throw IndexOutOfBoundsException
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(0));
    }

    @Test
    @DisplayName("TC02: call child(0) on element with exactly one Element child returns that child")
    public void test_TC02() {
        // GIVEN a parent with one element child (non-zero size, loop iterates once)
        Element parent = new Element("ul");
        Element only = parent.appendElement("li");
        // WHEN retrieving the first element child
        Element result = parent.child(0);
        // THEN we get exactly that appended element
        assertEquals(only, result);
    }

    @Test
    @DisplayName("TC03: call child(0) on element with mixed nodes returns first Element child")
    public void test_TC03() {
        // GIVEN a parent with mixed childNodes: TextNode then Element
        Element parent = new Element("p");
        parent.appendText("Hello"); // a TextNode filters out in child()
        Element span = parent.appendElement("span");
        // WHEN calling child(0), the loop should skip the TextNode and return the first Element
        Element result = parent.child(0);
        // THEN result is the span element
        assertEquals(span, result);
    }

    @Test
    @DisplayName("TC04: call child(1) on element with two Element children returns second child")
    public void test_TC04() {
        // GIVEN a parent with two element children (loop iterates twice)
        Element parent = new Element("ol");
        Element first = parent.appendElement("li");
        Element second = parent.appendElement("li");
        // WHEN retrieving index 1
        Element result = parent.child(1);
        // THEN we get the second appended child
        assertEquals(second, result);
    }

    @Test
    @DisplayName("TC05: call child(5) on element with two Element children throws IndexOutOfBoundsException")
    public void test_TC05() {
        // GIVEN a parent with two element children (loop builds list size=2)
        Element parent = new Element("ul");
        parent.appendElement("li");
        parent.appendElement("li");
        // WHEN calling child(5) (index > available elements) must throw
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(5));
    }

    @Test
    @DisplayName("TC06: call child(-1) on element with one Element child throws IndexOutOfBoundsException")
    public void test_TC06() {
        // GIVEN a parent with one element child (negative index triggers exception)
        Element parent = new Element("div");
        parent.appendElement("span");
        // WHEN calling child(-1), out-of-range negative index must throw
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(-1));
    }
}