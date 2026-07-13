package org.jsoup.nodes;

import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.ref.WeakReference;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for Element.children() cache behavior when shadowChildrenRef referent is cleared.
 */
public class Element_children_1_Test {

    @Test
    @DisplayName("TC07: children() rebuilds childElementsList when shadowChildrenRef referent is cleared")
    public void test_TC07() throws Exception {
        // GIVEN: an element with one child, and a primed shadowChildrenRef cache
        Element el = new Element("div");
        Element child = new Element("span");
        el.appendChild(child);
        // prime the cache: after this call, shadowChildrenRef != null and holds a list
        Elements initial = el.children();
        assertEquals(1, initial.size(), "Precondition: initial children list has one element");

        // use reflection to simulate that the weak reference referent has been cleared
        Field refField = Element.class.getDeclaredField("shadowChildrenRef");
        refField.setAccessible(true);
        // set a new WeakReference with a null referent to force rebuild path
        WeakReference<List<Element>> clearedRef = new WeakReference<>(null);
        refField.set(el, clearedRef);

        // WHEN: calling children() should detect null referent and rebuild the list
        Elements rebuilt = el.children();

        // THEN: assert that a fresh list containing the single child is returned
        assertEquals(1, rebuilt.size(), "Rebuilt children list should contain exactly one element after cache clear");
        assertEquals(child, rebuilt.get(0), "The rebuilt list's element should be the original child");
    }
}