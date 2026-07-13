package org.jsoup.nodes;

import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
public class Element_children_2_Test {

    @Test
    @DisplayName("TC07 children() returns cached list when userData cache present and modCount matches (cachedChildren non-null, no rebuild)")
    public void test_TC07() throws Exception {
        // GIVEN: an Element with two child Elements
        Element parent = new Element("ul");
        parent.appendElement("li");
        parent.appendElement("li");
        // reflect to access package-private cachedChildren()
        Method cachedChildren = Element.class.getDeclaredMethod("cachedChildren");
        cachedChildren.setAccessible(true);
        // before any children() call, cache should be null (no userData yet)
        assertNull(cachedChildren.invoke(parent), "Cache should be empty before first children() invocation");

        // WHEN: first children() populates the cache
        Elements first = parent.children();
        // retrieve cached list after first call
        @SuppressWarnings("unchecked")
        List<Element> cacheAfterFirst = (List<Element>) cachedChildren.invoke(parent);
        assertNotNull(cacheAfterFirst, "Cache should be populated after first children() call");
        assertEquals(2, first.size(), "First children() should return two elements");

        // WHEN: second children() should hit the cache without rebuilding
        Elements second = parent.children();
        @SuppressWarnings("unchecked")
        List<Element> cacheAfterSecond = (List<Element>) cachedChildren.invoke(parent);

        // THEN: the same cached list instance is used, no rebuild (cacheBefore == cacheAfter)
        assertSame(cacheAfterFirst, cacheAfterSecond, "Cache instance should be identical on second call when modCount unchanged");
        assertEquals(2, second.size(), "Second children() should still return two elements");
    }

    @Test
    @DisplayName("TC08 children() rebuilds list when cachedChildren returns stale reference due to forced null ref in userData")
    public void test_TC08() throws Exception {
        // GIVEN: an Element with two child Elements and a populated cache
        Element parent = new Element("div");
        parent.appendElement("p");
        parent.appendElement("span");
        // call children() to populate cache
        parent.children();
        // access Attributes.userData() map and clear the stored WeakReference to simulate stale cache
        Field childElsKeyField = Element.class.getDeclaredField("childElsKey");
        childElsKeyField.setAccessible(true);
        String childElsKey = (String) childElsKeyField.get(null);
        Map<String, Object> userData = parent.attributes().userData();
        userData.put(childElsKey, new WeakReference<List<Element>>(null));
        // verify that cachedChildren() now returns null (stale weak ref)
        Method cachedChildren = Element.class.getDeclaredMethod("cachedChildren");
        cachedChildren.setAccessible(true);
        assertNull(cachedChildren.invoke(parent), "Cache should be null when WeakReference referent is cleared");

        // WHEN: children() is called again, it should rebuild the list
        Elements rebuilt = parent.children();
        // THEN: a fresh list is built with two elements
        assertEquals(2, rebuilt.size(), "Rebuilt children() should return two elements after stale cache");
        assertNotNull(rebuilt.get(0), "First element of rebuilt list should not be null");
        assertNotNull(rebuilt.get(1), "Second element of rebuilt list should not be null");
    }
}