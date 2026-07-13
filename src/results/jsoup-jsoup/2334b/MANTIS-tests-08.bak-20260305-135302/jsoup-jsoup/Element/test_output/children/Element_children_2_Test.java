package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Element_children_2_Test {

    @Test
    @DisplayName("children() after emptying element clears cache and short-circuits on zero childNodeSize")
    public void test_TC07() {
        // GIVEN: an element with two <span> children, and initial children() call to populate the cache
        Element el = new Element("div");
        el.appendChild(new Element("span"));
        el.appendChild(new Element("span"));
        Elements initial = el.children(); // builds shadowChildrenRef with two spans (cache populated)
        assertEquals(2, initial.size(), "Precondition: initial children list should have two spans");

        // Clear all children, which should clear childNodes and invoke nodelistChanged -> cache invalidated
        el.empty(); // childNodes cleared and shadowChildrenRef reset to null

        // WHEN: calling children() a second time on an empty element
        Elements result = el.children();
        // B1->B2 short-circuits: childNodeSize is zero, so returns EmptyChildren directly without cache rebuild

        // THEN: the returned list is empty
        assertTrue(result.isEmpty(), "children() should return an empty list after emptying the element");
    }

    @Test
    @DisplayName("children() rebuilds list when shadowChildrenRef.get() returns null (simulate GC)")
    public void test_TC08() throws Exception {
        // GIVEN: an element with one <p> child, and initial children() call to populate the cache
        Element el = new Element("div");
        el.appendChild(new Element("p"));
        Elements first = el.children(); // builds shadowChildrenRef with one <p>
        assertEquals(1, first.size(), "Precondition: initial children list should have one <p>");
        assertEquals("p", first.get(0).tagName(), "Precondition: child tagName should be 'p'");

        // Simulate GC clearing the cache by setting the referent of shadowChildrenRef to null via reflection
        Field shadowField = Element.class.getDeclaredField("shadowChildrenRef");
        shadowField.setAccessible(true);
        // Replace existing WeakReference with one whose referent is null
        WeakReference<List<Element>> fakeRef = new WeakReference<>(null);
        shadowField.set(el, fakeRef);
        // At this point, shadowChildrenRef.get() == null, so children() must rebuild the list by iterating childNodes

        // WHEN: calling children() after simulating GC
        Elements result = el.children();

        // THEN: children() should rebuild a fresh list containing the single <p> element
        assertEquals(1, result.size(), "children() should rebuild and find one child element");
        assertEquals("p", result.get(0).tagName(), "Rebuilt child element should have tagName 'p'");
    }
}