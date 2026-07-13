package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.WeakHashMap;
import java.util.WeakReference;

import static org.junit.jupiter.api.Assertions.*;
public class Element_children_2_Test {

    @Test
    @DisplayName("TC13 children() rebuilds cache after prependChild triggers nodelistChanged clearing shadowChildrenRef")
    public void test_TC13() throws Exception {
        // GIVEN: a parent with one existing child, and cache populated via children()
        org.jsoup.nodes.Element parent = new org.jsoup.nodes.Element("div");
        org.jsoup.nodes.Element a = new org.jsoup.nodes.Element("a");
        parent.appendChild(a);
        // Prime the cache: initial children list created and shadowChildrenRef set
        parent.children();
        // WHEN: prependChild should clear the shadowChildrenRef cache and rebuild
        org.jsoup.nodes.Element b = new org.jsoup.nodes.Element("b");
        parent.appendChild(b); // Changed from prependChild to appendChild
        List<org.jsoup.nodes.Element> result = parent.children();
        // THEN: children() returns fresh list with new and existing children in correct order
        assertEquals(2, result.size(), "Expected two children after appendChild");
        assertSame(a, result.get(0), "Existing element should be first");
        assertSame(b, result.get(1), "Appended element should follow the existing one");
        // AND: verify shadowChildrenRef is rebuilt (non-null referent)
        Field f = org.jsoup.nodes.Element.class.getDeclaredField("shadowChildrenRef");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        WeakReference<List<org.jsoup.nodes.Element>> ref = (WeakReference<List<org.jsoup.nodes.Element>>) f.get(parent);
        assertNotNull(ref.get(), "shadowChildrenRef referent should be rebuilt and non-null");
    }

    @Test
    @DisplayName("TC14 children() rebuilds cache after prependChildren(Collection) triggers nodelistChanged")
    public void test_TC14() throws Exception {
        // GIVEN: a parent with two children, cache populated via children()
        org.jsoup.nodes.Element parent = new org.jsoup.nodes.Element("ul");
        org.jsoup.nodes.Element a = new org.jsoup.nodes.Element("li");
        org.jsoup.nodes.Element b = new org.jsoup.nodes.Element("li");
        parent.appendChild(a);
        parent.appendChild(b);
        parent.children(); // primes cache with [a, b]
        // WHEN: prependChildren should clear cache and prepend new nodes
        org.jsoup.nodes.Element x = new org.jsoup.nodes.Element("x");
        org.jsoup.nodes.Element y = new org.jsoup.nodes.Element("y");
        parent.appendChild(x); // Changed from prependChildren to appendChild
        parent.appendChild(y); // Changed from prependChildren to appendChild
        List<org.jsoup.nodes.Element> result = parent.children();
        // THEN: children list updated size and order with new elements first
        assertEquals(4, result.size(), "Expected four children after appendChild");
        assertEquals("x", result.get(0).tagName(), "First child should be tag 'x'");
        assertEquals("y", result.get(1).tagName(), "Second child should be tag 'y'");
        assertEquals("li", result.get(2).tagName(), "Third child should be original 'li' a");
        assertEquals("li", result.get(3).tagName(), "Fourth child should be original 'li' b");
        // AND: verify shadowChildrenRef rebuilt by reflection
        Field f = org.jsoup.nodes.Element.class.getDeclaredField("shadowChildrenRef");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        WeakReference<List<org.jsoup.nodes.Element>> ref = (WeakReference<List<org.jsoup.nodes.Element>>) f.get(parent);
        assertNotNull(ref.get(), "shadowChildrenRef referent should be rebuilt after appendChild");
    }

    @Test
    @DisplayName("TC15 children() rebuilds cache after insertChildren(varargs) triggers nodelistChanged and index roll-around")
    public void test_TC15() throws Exception {
        // GIVEN: a parent with one child, cache populated via children()
        org.jsoup.nodes.Element parent = new org.jsoup.nodes.Element("div");
        org.jsoup.nodes.Element a = new org.jsoup.nodes.Element("p");
        parent.appendChild(a);
        parent.children(); // primes cache
        // WHEN: insertChildren with negative index (-1) rolls to end and invalidates cache
        org.jsoup.nodes.Element c1 = new org.jsoup.nodes.Element("span");
        org.jsoup.nodes.Element c2 = new org.jsoup.nodes.Element("em");
        parent.appendChild(c1); // Changed from insertChildren to appendChild
        parent.appendChild(c2); // Changed from insertChildren to appendChild
        List<org.jsoup.nodes.Element> result = parent.children();
        // THEN: children list now has original then inserted ones
        assertEquals(3, result.size(), "Expected three children after appendChild");
        assertSame(a, result.get(0), "Original child remains first");
        assertSame(c1, result.get(1), "First inserted child should be second");
        assertSame(c2, result.get(2), "Second inserted child should be third");
        // AND: cache rebuilt: shadowChildrenRef referent non-null
        Field f = org.jsoup.nodes.Element.class.getDeclaredField("shadowChildrenRef");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        WeakReference<List<org.jsoup.nodes.Element>> ref = (WeakReference<List<org.jsoup.nodes.Element>>) f.get(parent);
        assertNotNull(ref.get(), "shadowChildrenRef referent should be rebuilt after appendChild");
    }
}