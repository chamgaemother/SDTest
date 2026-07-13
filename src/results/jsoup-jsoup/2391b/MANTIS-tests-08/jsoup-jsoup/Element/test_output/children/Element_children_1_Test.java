package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.select.Elements;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.WeakHashMap;
import java.lang.ref.WeakReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for Element.children() cache behavior.
 */
public class Element_children_1_Test {

    @Test
    @DisplayName("children() returns cached child list on second invocation without modifications (cache-hit branch)")
    public void test_TC09() throws Exception {
        // GIVEN: parent element with two children
        Element parent = new Element("div");
        Element c1 = new Element("span");
        Element c2 = new Element("p");
        parent.appendChild(c1);
        parent.appendChild(c2);
        // Use reflection to get the private key for child elements cache
        Field keyField = Element.class.getDeclaredField("childElsKey");
        keyField.setAccessible(true);
        String cacheKey = (String) keyField.get(null);
        // PRECONDITION: call children() once to populate the cache (B0→B1→B4→B5)
        Elements firstCall = parent.children();
        // retrieve the stashed referent list via userData
        @SuppressWarnings("unchecked")
        WeakReference<?> ref1 = (WeakReference<?>) parent.attributes().userData().get(cacheKey);
        Object cachedList1 = ref1.get();
        assertNotNull(cachedList1, "Cache should hold a reference after first children() call");
        // WHEN: call children() second time without intervening modifications
        Elements secondCall = parent.children();
        @SuppressWarnings("unchecked")
        WeakReference<?> ref2 = (WeakReference<?>) parent.attributes().userData().get(cacheKey);
        Object cachedList2 = ref2.get();
        // THEN: the cached list instance should be the same (identity) across calls
        assertSame(cachedList1, cachedList2,
            "Expected the same cached list instance on second children() invocation without modifications");
    }

    @Test
    @DisplayName("children() refreshes cache when childNodes modCount mismatches stash (cache-invalidate branch)")
    public void test_TC10() throws Exception {
        // GIVEN: parent element with one child, cache populated
        Element parent = new Element("ul");
        Element c1 = new Element("li");
        parent.appendChild(c1);
        // Populate cache
        Elements original = parent.children();
        // The cache now contains one child element
        assertEquals(1, original.size(), "Precondition: only one child in cache");
        assertTrue(original.contains(c1), "Precondition: cache contains the existing child");

        // WHEN: append a new child, which increments modCount and should invalidate cache (B0→B1→B4→B3(loop×3)→B5)
        Element c2 = new Element("li");
        parent.appendChild(c2);
        Elements updated = parent.children();

        // THEN: children() after appending should reflect both children
        assertAll("updated children list",
            () -> assertEquals(2, updated.size(), "Updated children list must contain two elements"),
            () -> assertTrue(updated.contains(c1), "Updated list must contain the first child"),
            () -> assertTrue(updated.contains(c2), "Updated list must contain the newly appended child")
        );
    }
}