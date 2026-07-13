package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for Element.child(int) method covering various branches and boundary conditions.
 */
public class Element_child_0_Test {

    @Test
    @DisplayName("TC01: child(0) on element with no children throws IndexOutOfBoundsException (childNodeSize==0)")
    public void test_TC01() {
        // GIVEN an Element with no children (ensure childNodeSize()==0 branch)
        Element parent = new Element("div");
        // WHEN & THEN: expecting IndexOutOfBoundsException because there are no element children
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(0));
    }

    @Test
    @DisplayName("TC02: child(0) returns the only Element child (one-element loop)")
    public void test_TC02() {
        // GIVEN an Element with exactly one Element child (childNodeSize>0, loop runs once)
        Element parent = new Element("div");
        Element span = new Element("span");
        parent.appendChild(span);
        // WHEN: retrieve the first Element child
        Element result = parent.child(0);
        // THEN: result should be the only child with tagName "span"
        assertEquals("span", result.tagName());
    }

    @Test
    @DisplayName("TC03: child(2) returns third Element among multiple children (three-element loop)")
    public void test_TC03() {
        // GIVEN an Element with three <li> children (childNodeSize>0, loop runs three times)
        Element parent = new Element("ul");
        Element first = new Element("li");
        Element second = new Element("li");
        Element third = new Element("li");
        parent.appendChild(first);
        parent.appendChild(second);
        parent.appendChild(third);
        // WHEN: retrieve the third element child by index
        Element result = parent.child(2);
        // THEN: expected to be the same instance as parent.children().get(2)
        assertSame(parent.children().get(2), result);
    }

    @Test
    @DisplayName("TC04: child(0) ignores non-Element nodes and returns first Element child (mixed children)")
    public void test_TC04() {
        // GIVEN an Element with mixed children: a TextNode then an Element
        Element parent = new Element("section");
        TextNode text = new TextNode("text");
        Element article = new Element("article");
        parent.appendChild(text);      // non-Element child first
        parent.appendChild(article);   // first element child should be 'article'
        // WHEN: retrieve the first element child (index 0)
        Element result = parent.child(0);
        // THEN: result should be the 'article' element, skipping the text node
        assertEquals("article", result.tagName());
    }

    @Test
    @DisplayName("TC05: child(-1) negative index throws IndexOutOfBoundsException after adjustment")
    public void test_TC05() {
        // GIVEN an Element with one child (childNodeSize>0)
        Element parent = new Element("div");
        parent.appendChild(new Element("p"));
        // WHEN & THEN: negative index -1 adjusted to last position but out of bounds for element-only list -> exception
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(-1));
    }
}