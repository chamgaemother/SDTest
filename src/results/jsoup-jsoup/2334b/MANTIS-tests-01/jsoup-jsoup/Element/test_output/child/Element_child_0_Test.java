package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * JUnit 5 tests for Element.child(int) method scenarios TC01-TC05.
 */
public class Element_child_0_Test {

    @Test
    @DisplayName("child(0) on element with no child nodes throws IndexOutOfBoundsException (childNodeSize==0)")
    public void test_TC01() {
        // GIVEN: a div element with no children (childNodes is empty)  -> branch B1 true fails get index
        Element parent = new Element("div");
        // WHEN & THEN: expecting IndexOutOfBoundsException for accessing child(0) on empty list
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(0));
    }

    @Test
    @DisplayName("child(0) returns the single child Element when exactly one Element child exists (loop iterates once)")
    public void test_TC02() {
        // GIVEN: a ul element with exactly one Element child -> branch B1 false, B3 path, loop executes once
        Element parent = new Element("ul");
        Element li = new Element("li");
        parent.appendChild(li);
        // WHEN: request the first element child
        Element result = parent.child(0);
        // THEN: the result should be the exact same instance appended -> return value equals li
        assertEquals(li, result);
    }

    @Test
    @DisplayName("child(1) on element with two children (one TextNode and one Element) returns second child Element (loop iterates twice filtering)")
    public void test_TC03() {
        // GIVEN: a div with one TextNode and then one Element -> branch B1 false, B3 path, loop filters non-Element then finds element
        Element parent = new Element("div");
        parent.appendText("text"); // adds a TextNode, ensures loop iteration on non-Element child
        Element span = new Element("span");
        parent.appendChild(span);
        // WHEN: request the second element child by index 1
        Element result = parent.child(1);
        // THEN: the result should be the same span instance -> correct filtering over mixed nodes
        assertEquals(span, result);
    }

    @Test
    @DisplayName("child(-1) on element with one Element child throws IndexOutOfBoundsException (negative index out of range)")
    public void test_TC04() {
        // GIVEN: a p element with one Element child -> branch B1 false, B3 path, negative index check should fail
        Element parent = new Element("p");
        Element c = new Element("b");
        parent.appendChild(c);
        // WHEN & THEN: negative index -1 is out of bounds for element children -> expect IndexOutOfBoundsException
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(-1));
    }

    @Test
    @DisplayName("child(2) on element with three non-Element nodes throws IndexOutOfBoundsException after loop filters out all nodes (loop iterates N)")
    public void test_TC05() {
        // GIVEN: a div with three TextNode children and no Element children -> branch B1 false, B3 path, loop iterates 3, filters all
        Element parent = new Element("div");
        parent.appendText("a");
        parent.appendText("b");
        parent.appendText("c");
        // WHEN & THEN: requesting child index 2 (third element child) but no elements exist -> expect IndexOutOfBoundsException
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(2));
    }
}