package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.ref.WeakReference;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Element_children_1_Test {

    @Test
    @DisplayName("TC11: children() rebuilds cache when existing shadowChildrenRef get() returns null")
    public void test_TC11() throws Exception {
        // GIVEN: parent with one child and cache primed, then simulate GC clearing the WeakReference
        Element parent = new Element("div");                        // branch B0→B3: no children, so childNodeSize()==1 after append
        Element child = new Element("span");
        parent.appendChild(child);
        // prime the cache: branch B6 storing shadowChildrenRef
        Elements first = parent.children();
        assertEquals(1, first.size(), "cache should contain the one child");
        // reflectively clear the cached reference to simulate GC cleared referent
        Field refField = Element.class.getDeclaredField("shadowChildrenRef");
        refField.setAccessible(true);
        @SuppressWarnings("unchecked")
        WeakReference<List<Element>> ref = (WeakReference<List<Element>>) refField.get(parent);
        ref.clear(); // now ref.get() == null → force rebuild path B3→B6→B3→B5

        // WHEN: call children() again, expecting rebuild
        Elements result = parent.children(); // branch takes cleared ref path

        // THEN: get fresh list with the child, and new non-null weak reference stored
        assertEquals(1, result.size(), "after rebuild, list should still contain the one child");
        assertSame(child, result.get(0), "the child in rebuilt list must be the same instance");
        @SuppressWarnings("unchecked")
        WeakReference<List<Element>> newRef = (WeakReference<List<Element>>) refField.get(parent);
        assertNotNull(newRef, "shadowChildrenRef field should be updated to a new WeakReference");
        assertNotNull(newRef.get(), "new WeakReference referent must be non-null after rebuild");
    }

    @Test
    @DisplayName("TC12: children() invalidates cache after child removal via Node.remove()")
    public void test_TC12() throws Exception {
        // GIVEN: parent with two children, cache populated
        Element parent = new Element("ul");
        Element a = new Element("li");
        Element b = new Element("li");
        parent.appendChild(a);
        parent.appendChild(b);
        // prime cache: branch B6 storing shadowChildrenRef
        Elements first = parent.children();
        assertEquals(2, first.size(), "initial cache should have two children");

        // WHEN: remove one child via Node.remove() triggering nodelistChanged → clear cache (branch B4)
        a.remove(); // this detaches a and should clear shadowChildrenRef 
        // children() should take B0→B3 with no cached list, then rebuild B6
        Elements result = parent.children();

        // THEN: only remaining child b is returned, and cache rebuilt
        assertEquals(1, result.size(), "after removal, only one child should remain");
        assertSame(b, result.get(0), "remaining child must be b");
        Field f = Element.class.getDeclaredField("shadowChildrenRef");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        WeakReference<List<Element>> postRef = (WeakReference<List<Element>>) f.get(parent);
        assertNotNull(postRef, "shadowChildrenRef should be non-null after rebuild");
        assertNotNull(postRef.get(), "referent should be non-null after rebuild");
    }
}