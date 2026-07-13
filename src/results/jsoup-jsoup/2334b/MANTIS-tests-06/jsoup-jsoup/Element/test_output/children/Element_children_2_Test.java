package org.jsoup.nodes;

import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.ref.WeakReference;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Element_children_2_Test {

    @Test
    @DisplayName("TC07: children() returns empty list via childNodeSize()==0 shortcut after cache rebuild and node removal")
    public void test_TC07() {
        // GIVEN: an element with one child, cache populated, then emptied
        Element el = new Element("div");
        el.appendElement("span");               // childNodeSize > 0, shadowChildrenRef built on first children()
        el.children();                          // populate shadowChildrenRef cache (branch B3)
        el.empty();                             // clear childNodes and invalidate cache (branch B5)

        // WHEN: calling children() again should hit the childNodeSize()==0 shortcut (branch B0→B3→B5)
        Elements result = el.children();

        // THEN: the result must be an empty list
        assertEquals(0, result.size(), "Expected no child elements after empty()");
    }

    @Test
    @DisplayName("TC08: children() rebuilds list when shadowChildrenRef exists but referent has been cleared")
    public void test_TC08() throws Exception {
        // GIVEN: an element with one child, cache populated, then simulate GC clearing referent
        Element el = new Element("div");
        Element child = el.appendElement("p");  // childNodeSize > 0
        el.children();                          // populate shadowChildrenRef (cache miss then built)

        // Use reflection to replace shadowChildrenRef with one pointing to null (simulate GC cleared)
        Field shadowField = Element.class.getDeclaredField("shadowChildrenRef");
        shadowField.setAccessible(true);
        // Set to a WeakReference whose referent is already cleared (null)
        shadowField.set(el, new WeakReference<List<Element>>(null));

        // WHEN: calling children() should detect missing referent and rebuild the list (branch B3 miss→rebuild→B5)
        Elements result = el.children();

        // THEN: the rebuilt list must contain exactly the original child
        assertEquals(1, result.size(), "Expected one child after rebuilding cache");
        assertSame(child, result.get(0), "Expected the rebuilt child to be the same instance");
    }
}