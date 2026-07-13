package org.jsoup.nodes;

import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Element_children_1_Test {

    @Test
    @DisplayName("TC05: children() after cache build and structural change should invalidate cache and rebuild filtered list")
    public void test_TC05() {
        // GIVEN a parent with one child and cache built
        Element parent = new Element("div");
        Element first = new Element("span");
        parent.appendChild(first);
        // first call builds and caches shadowChildrenRef via childElementsList (path enters B3→B8 loop)
        Elements initial = parent.children();
        assertEquals(1, initial.size(), "Initial children list should contain exactly the first child");

        // WHEN a structural change occurs: appendChild should call nodelistChanged and clear shadowChildrenRef
        Element secondChild = new Element("a");
        parent.appendChild(secondChild);
        // inline comment: appendChild triggers NodeList.onContentsChanged -> nodelistChanged, clearing shadowChildrenRef

        // THEN children() must rebuild cache and include both children
        Elements updated = parent.children();
        assertAll("Updated children list must have both original and new child, in correct order",
            () -> assertEquals(2, updated.size(), "Expected two children after cache invalidation and rebuild"),
            () -> assertSame(first, updated.get(0), "First child should remain the original <span> element"),
            () -> assertSame(secondChild, updated.get(1), "Second child should be the newly appended <a> element")
        );
    }

    @Test
    @DisplayName("TC06: children() when shadowChildrenRef exists but referent was GCed should rebuild filtered list")
    public void test_TC06() throws Exception {
        // GIVEN a parent with two children and a synthetic cleared WeakReference
        Element parent = new Element("ul");
        Element li1 = new Element("li");
        Element li2 = new Element("li");
        parent.appendChild(li1);
        parent.appendChild(li2);
        // build initial cache to get shadowChildrenRef non-null
        Elements beforeGc = parent.children();
        assertEquals(2, beforeGc.size(), "Precondition: two children should be present");

        // Use reflection to set shadowChildrenRef to a WeakReference with null referent
        Field refField = Element.class.getDeclaredField("shadowChildrenRef");
        refField.setAccessible(true);
        // inline comment: simulate GC clearing the referent
        refField.set(parent, new WeakReference<List<Element>>(null));

        // WHEN children() is called, detect null referent and rebuild filtered list
        Elements result = parent.children();

        // THEN result must contain both elements
        assertEquals(2, result.size(), "children() should rebuild and return both elements when weakref referent is null");
        assertTrue(result.contains(li1) && result.contains(li2),
                   "Resulting list should include both li1 and li2");
    }
}