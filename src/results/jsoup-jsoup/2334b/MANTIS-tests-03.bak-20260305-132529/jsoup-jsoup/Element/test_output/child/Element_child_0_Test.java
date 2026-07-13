package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class Element_child_0_Test {

    @Test
    @DisplayName("TC01: child(0) on element with no child nodes returns IndexOutOfBoundsException (childNodeSize()==0 branch)")
    public void test_TC01() {
        // GIVEN an element with no children: triggers childNodeSize()==0 branch
        Element parent = new Element("div");
        // WHEN/THEN: expect IndexOutOfBoundsException and no change to childNodes size
        IndexOutOfBoundsException ex = assertThrows(IndexOutOfBoundsException.class, () -> {
            parent.child(0);
        });
        // verify no mutation: still zero element children
        assertEquals(0, parent.childrenSize(), "Child nodes should remain empty after exception");
    }

    @Test
    @DisplayName("TC02: child(0) returns first Element when exactly one Element child exists (childNodeSize()>0, one instance in loop)")
    public void test_TC02() {
        // GIVEN a parent with one <span> child: childNodeSize()>0 and loop executes once adding the element
        Element parent = new Element("div");
        Element spanChild = parent.appendElement("span");
        // WHEN: retrieve the first child element
        Element result = parent.child(0);
        // THEN: it should be the same <span> element
        assertEquals("span", result.tagName(), "Expected the tag name of the first child to be 'span'");
        assertSame(spanChild, result, "Should return the exact appended span element instance");
    }

    @Test
    @DisplayName("TC03: child(1) returns second Element among mixed child nodes, skipping non-Element nodes (loop filters and adds only Element instances)")
    public void test_TC03() {
        // GIVEN a parent with mixed nodes: a TextNode then two <li> elements
        Element parent = new Element("ul");
        parent.appendText("text"); // non-Element, should be skipped in filtering
        Element firstLi = parent.appendElement("li");
        Element secondLi = parent.appendElement("li");
        // WHEN: request second element child at index 1 of filtered list
        Element result = parent.child(1);
        // THEN: should return the second <li> element, not the text node
        assertEquals("li", result.tagName(), "Expected the tag name of the second child to be 'li'");
        assertSame(secondLi, result, "Should return the second appended li element instance");
    }

    @Test
    @DisplayName("TC04: child(-1) with negative index beyond start throws IndexOutOfBoundsException (list.get negative index)")
    public void test_TC04() {
        // GIVEN a parent with a single <p> child: childElementsList size == 1
        Element parent = new Element("div");
        parent.appendElement("p");
        // WHEN/THEN: negative index should lead to IndexOutOfBoundsException
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(-1));
        // ensure no side-effects on children
        assertEquals(1, parent.childrenSize(), "Children size should remain unchanged after exception");
    }
}