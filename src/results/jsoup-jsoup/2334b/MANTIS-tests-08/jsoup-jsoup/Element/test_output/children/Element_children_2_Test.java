package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.ref.WeakReference;

import static org.junit.jupiter.api.Assertions.*;
public class Element_children_2_Test {

    @Test
    @DisplayName("children() reuses cached empty list for element with only non-element nodes (shadowChildrenRef non-null empty branch)")
    public void test_TC10() {
        // GIVEN an element with only a TextNode child -> children list will be empty but cached
        Element el = new Element("p");
        el.appendChild(new TextNode("t"));
        // first invocation builds and caches an empty list (shadowChildrenRef points to empty list)
        Elements first = el.children();
        // WHEN children() is called again -> should reuse same cached empty Elements instance
        Elements second = el.children();
        // THEN both references should be identical, indicating cache reuse
        assertSame(first, second, "Expected the same cached empty Elements instance on second call");
    }

    @Test
    @DisplayName("children() rebuilds when cached shadowChildrenRef referent has been cleared (weakref-null branch)")
    public void test_TC11() throws Exception {
        // GIVEN an element with one Element child -> initial children() caches shadowChildrenRef
        Element el = new Element("div");
        Element child = new Element("span");
        el.appendChild(child);
        // first call to populate shadowChildrenRef with a real list
        Elements initial = el.children();
        assertEquals(1, initial.size(), "Precondition: initial children list must contain one element");

        // MANUALLY clear the referent via reflection to simulate GC clearing the weak reference
        Field refField = Element.class.getDeclaredField("shadowChildrenRef");
        refField.setAccessible(true);
        // set a WeakReference whose get() returns null
        WeakReference<?> emptyRef = new WeakReference<>(null);
        refField.set(el, emptyRef);

        // WHEN children() is called after the referent is cleared -> should rebuild the list
        Elements result = el.children();

        // THEN the returned list should contain the one child element
        assertEquals(1, result.size(), "Expected rebuilt children list size to be 1 after clearing cache");
        assertEquals("span", result.get(0).tagName(), "Expected child tagName to match the appended child");
    }
}