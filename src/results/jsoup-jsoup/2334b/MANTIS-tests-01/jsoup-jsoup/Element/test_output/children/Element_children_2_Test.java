package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.ref.WeakReference;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Element_children_2_Test {

    @Test
    @DisplayName("children() with only text nodes caches empty result on first call and reuses underlying list referent on second call (referent non-null path)")
    public void test_TC08() throws Exception {
        // GIVEN: an element with only a text node child (no element children)
        Element el = new Element("p");
        el.appendText("data");
        // WHEN: first children() call, should detect no element children and set cache to empty list
        Elements first = el.children(); // B3(false) → B6(false): childNodeSize>0 but no Element instances, so no cache yet
        // THEN: first result is empty
        assertEquals(0, first.size(), "First children() call should return empty list");

        // WHEN: second children() call, should reuse the cached empty list (referent non-null path)
        Elements second = el.children(); // B3(false) → B6(true): cached shadowChildrenRef.get() non-null
        // THEN: second result also empty
        assertEquals(0, second.size(), "Second children() call should return empty list using cache");

        // AND: underlying shadowChildrenRef referent is non-null (cache reused)
        Field shadowField = Element.class.getDeclaredField("shadowChildrenRef");
        shadowField.setAccessible(true);
        @SuppressWarnings("unchecked")
        WeakReference<List<Element>> ref = (WeakReference<List<Element>>) shadowField.get(el);
        // The underlying cached list referent should not be cleared
        assertNotNull(ref, "shadowChildrenRef should have been initialized");
        assertNotNull(ref.get(), "Cached child elements list referent should be non-null after second call");
    }

    @Test
    @DisplayName("children() recomputes after children cleared via empty(), invalidating cache and returning empty list")
    public void test_TC09() throws Exception {
        // GIVEN: an element with two child elements, so cache is primed after first call
        Element el = new Element("div");
        el.appendChild(new Element("a"));
        el.appendChild(new Element("b"));
        Elements initial = el.children(); // prime the cache with two elements
        assertEquals(2, initial.size(), "Priming children() call should return two elements");

        // Store initial cached referent to compare later
        Field shadowField = Element.class.getDeclaredField("shadowChildrenRef");
        shadowField.setAccessible(true);
        @SuppressWarnings("unchecked")
        WeakReference<List<Element>> beforeRef = (WeakReference<List<Element>>) shadowField.get(el);
        assertNotNull(beforeRef.get(), "Cache referent should be non-null after priming");

        // WHEN: empty() clears children and triggers nodelistChanged (invalidates cache)
        el.empty(); // triggers nodelistChanged, should nullify shadowChildrenRef internally
        
        // THEN: children() recomputes and returns empty list
        Elements result = el.children();
        assertEquals(0, result.size(), "children() after empty() should return empty list");

        // AND: cache referent updated (old referent should not be reused)
        @SuppressWarnings("unchecked")
        WeakReference<List<Element>> afterRef = (WeakReference<List<Element>>) shadowField.get(el);
        // After clearing, either referent is new or empty but should not be the same object as before
        assertNotNull(afterRef, "shadowChildrenRef field should exist");
        assertNotSame(beforeRef.get(), afterRef.get(), "Cache referent should be invalidated and recomputed");
    }
}