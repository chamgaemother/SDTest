package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.ref.WeakReference;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
public class Element_child_1_Test {

    @Test
    @DisplayName("TC08: Invoking child(0) twice triggers the cached childElementsList path on second call")
    public void test_TC08() throws Exception {
        // GIVEN an Element parent with one Element child, no prior shadowChildrenRef
        Element parent = new Element("div");
        Element child = parent.appendElement("p");
        // Precondition: shadowChildrenRef is initially null
        Field shadowRefField = Element.class.getDeclaredField("shadowChildrenRef");
        shadowRefField.setAccessible(true);
        Object initialRef = shadowRefField.get(parent);
        // ensure no cache yet
        assertEquals(null, initialRef, "shadowChildrenRef should start null before any child() call");

        // WHEN first child(0) invocation: builds the childElementsList and caches it
        Element first = parent.child(0);
        // THEN first call returns the appended child
        assertSame(child, first, "First call to child(0) should return the appended child");

        // After first call, shadowChildrenRef should be non-null and refer to a list containing our child
        @SuppressWarnings("unchecked")
        WeakReference<List<Element>> afterFirstRef = (WeakReference<List<Element>>) shadowRefField.get(parent);
        List<Element> cachedList = afterFirstRef.get();
        // Inline comment: we expect B3 loop to run, build list once and store in shadowChildrenRef
        assertEquals(1, cachedList.size(), "After first call, cached child list should have one element");
        assertSame(child, cachedList.get(0), "Cached list's first element should be our child");

        // WHEN second child(0) invocation: should use the cached list without rebuilding
        Element second = parent.child(0);
        // THEN second call returns the same child from the cache
        assertSame(child, second, "Second call to child(0) should return the same child from cache, not rebuild");
        // also the shadowChildrenRef should not have changed reference
        @SuppressWarnings("unchecked")
        WeakReference<List<Element>> afterSecondRef = (WeakReference<List<Element>>) shadowRefField.get(parent);
        assertSame(afterFirstRef, afterSecondRef, "shadowChildrenRef should remain the same WeakReference instance");
    }
}