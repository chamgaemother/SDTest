package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.Comment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.ref.WeakReference;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Element_child_0_Test {

    @Test
    @DisplayName("TC01: child(0) on element with no children should throw IndexOutOfBoundsException (childNodeSize == 0 branch)")
    public void test_TC01() {
        // GIVEN an element with no children (childNodeSize == 0)
        Element parent = new Element("div");
        // WHEN & THEN calling child(0) should throw IndexOutOfBoundsException
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(0));
    }

    @Test
    @DisplayName("TC02: child(-1) on element with one child should throw IndexOutOfBoundsException (negative index boundary)")
    public void test_TC02() {
        // GIVEN an element with exactly one child
        Element parent = new Element("div");
        parent.appendChild(new Element("span"));
        // WHEN & THEN calling child(-1) should throw IndexOutOfBoundsException due to negative index
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(-1));
    }

    @Test
    @DisplayName("TC03: child(0) returns only element child when childNodes contains mixed node types (filters out non-Element)")
    public void test_TC03() {
        // GIVEN an element with mixed children: TextNode, Element, Comment
        Element parent = new Element("div");
        parent.appendChild(new TextNode("text"));
        Element p = new Element("p");
        parent.appendChild(p);
        parent.appendChild(new Comment("c"));
        // WHEN retrieving the first element child (filters out non-Element nodes)
        Element child = parent.child(0);
        // THEN the returned element is the <p> instance
        assertEquals("p", child.tagName());
        assertSame(p, child);
    }

    @Test
    @DisplayName("TC04: child(0) returns first element when exactly one element child present")
    public void test_TC04() {
        // GIVEN an element with a single Element child
        Element parent = new Element("ul");
        Element li = new Element("li");
        parent.appendChild(li);
        // WHEN retrieving child(0)
        Element child = parent.child(0);
        // THEN should return exactly the same instance
        assertSame(li, child);
    }

    @Test
    @DisplayName("TC05: child(1) returns second element when multiple element children present")
    public void test_TC05() {
        // GIVEN an element with two <li> children
        Element parent = new Element("ol");
        parent.appendChild(new Element("li"));
        Element second = parent.appendElement("li");
        // WHEN retrieving the second element child
        Element child = parent.child(1);
        // THEN should return the second <li> instance
        assertSame(second, child);
    }

    @Test
    @DisplayName("TC06: child(2) with index equal to filtered element count should throw IndexOutOfBoundsException (upper bound)")
    public void test_TC06() {
        // GIVEN an element with two element children
        Element parent = new Element("div");
        parent.appendChild(new Element("span"));
        parent.appendChild(new Element("a"));
        // WHEN & THEN child(2) is out of bounds (only indices 0 and 1 valid)
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(2));
    }

    @Test
    @DisplayName("TC07: child(0) after caching ensures shadowChildrenRef is reused on subsequent calls")
    public void test_TC07() throws Exception {
        // GIVEN an element with one <span> child to build the cache
        Element parent = new Element("div");
        Element span = parent.appendElement("span");
        // WHEN calling child(0) twice
        Element first = parent.child(0);
        Element second = parent.child(0);
        // THEN the same instance is returned both times
        assertSame(first, second);
        assertSame(span, first);
        // AND the private cache field shadowChildrenRef should be non-null and hold our list
        Field f = Element.class.getDeclaredField("shadowChildrenRef");
        f.setAccessible(true);
        Object refObj = f.get(parent);
        assertNotNull(refObj, "shadowChildrenRef should be initialized");
        assertTrue(refObj instanceof WeakReference, "shadowChildrenRef must be a WeakReference");
        @SuppressWarnings("unchecked")
        WeakReference<List<Element>> weakRef = (WeakReference<List<Element>>) refObj;
        List<Element> cachedList = weakRef.get();
        assertNotNull(cachedList, "cached child elements list should not be cleared");
        assertEquals(1, cachedList.size(), "cached list size should reflect one element child");
        assertSame(span, cachedList.get(0), "cached list must contain the same span element");
    }
}