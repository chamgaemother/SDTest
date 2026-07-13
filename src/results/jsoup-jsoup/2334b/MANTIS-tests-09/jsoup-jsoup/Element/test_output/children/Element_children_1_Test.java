package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Element_children_1_Test {

    @Test
    @DisplayName("TC05: children() returns cached child list on second call without intervening mutations (cache-hit branch)")
    public void test_TC05() throws Exception {
        // GIVEN: an element with two child <span> elements. This ensures childElementsList builds cache of size 2.
        Element el = new Element("div");
        el.appendChild(new Element("span")); // first child triggers ensureChildNodes and cache build
        el.appendChild(new Element("span")); // second child populates cache list

        // Use reflection to access shadowChildrenRef before any children() call: should be null
        Field shadowField = Element.class.getDeclaredField("shadowChildrenRef");
        shadowField.setAccessible(true);
        WeakReference<?> initialRef = (WeakReference<?>) shadowField.get(el);
        assertNull(initialRef, "Before first children(), shadowChildrenRef should be null indicating no cache");

        // WHEN: call children() twice without mutations
        Elements first = el.children(); // builds and caches childElementsList
        Elements second = el.children(); // should hit cache, not rebuild

        // THEN: both calls return Elements of size 2 as intended
        assertAll(
            () -> assertEquals(2, first.size(), "First children() call should return two child elements"),
            () -> assertEquals(2, second.size(), "Second children() call should return two child elements")
        );

        // AND: verify cache hit by inspecting shadowChildrenRef; both referents should be identical
        @SuppressWarnings("unchecked")
        WeakReference<List<Element>> cachedRef = (WeakReference<List<Element>>) shadowField.get(el);
        assertNotNull(cachedRef, "After first children(), shadowChildrenRef should be set to a WeakReference caching the list");
        List<Element> cachedList1 = cachedRef.get();
        assertNotNull(cachedList1, "Cached list should be non-null after first call");

        // Calling children() again should reuse same cached list instance
        WeakReference<List<Element>> cachedRefAfterSecond = (WeakReference<List<Element>>) shadowField.get(el);
        List<Element> cachedList2 = cachedRefAfterSecond.get();
        // The referent should be the same list instance, indicating cache hit (no rebuild)
        assertSame(cachedList1, cachedList2, "Cache hit: underlying childElementsList instances should be identical across calls");
    }
}