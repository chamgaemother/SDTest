package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.ref.WeakReference;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for Element.child(int).
 * Covers cache invalidation and rebuild of shadowChildrenRef.
 */
public class Element_child_2_Test {

    @Test
    @DisplayName("TC08: child(1) after inserting new node invalidates shadowChildrenRef cache and rebuilds list")
    public void test_TC08() throws Exception {
        // GIVEN an Element parent with two element children
        Element parent = new Element("div");
        Element e1 = new Element("span");
        Element e2 = new Element("p");
        parent.appendChild(e1);  // B3: ensureChildNodes creates childNodes from EmptyNodes
        parent.appendChild(e2);  // B5: childElementsList builds shadowChildrenRef first time

        // WHEN first call to child(1) builds and caches the element list
        Element first = parent.child(1);
        // inline comment: childNodeSize != 0 so B3->B5->B6->B8 path executed

        // WHEN appending a TextNode to trigger nodelistChanged() and clear cache
        parent.appendChild(new TextNode("new"));
        // inline comment: appendChild on non-Element still calls ensureChildNodes and nodelistChanged()
        // shadowChildrenRef should be null after this change

        // WHEN second call to child(1) should rebuild cache and return same logical child
        Element second = parent.child(1);
        // inline comment: new cache built again via B5->B6->B8

        // THEN both calls return the original second element
        assertSame(e2, first, "First call to child(1) must return the second appended element e2");
        assertSame(e2, second, "Second call after mutation must still return the same element e2");

        // THEN verify that the cache instance was invalidated and a new list created
        Field refField = Element.class.getDeclaredField("shadowChildrenRef");
        refField.setAccessible(true);
        @SuppressWarnings("unchecked")
        WeakReference<List<Element>> afterRef = (WeakReference<List<Element>>) refField.get(parent);
        assertNotNull(afterRef, "shadowChildrenRef field should not be null after rebuilding cache");
        assertNotNull(afterRef.get(), "WeakReference<List<Element>> should refer to a non-null list after rebuild");
    }
}