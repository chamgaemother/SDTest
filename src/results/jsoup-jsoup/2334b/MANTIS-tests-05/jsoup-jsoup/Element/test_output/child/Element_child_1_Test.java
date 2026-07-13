package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.ref.WeakReference;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
public class Element_child_1_Test {

    @Test
    @DisplayName("child(1) invoked twice uses cached shadowChildrenRef on second call")
    public void test_TC07() throws Exception {
        // GIVEN a parent element with two child elements and no prior childElementsList cache
        Element parent = new Element("div"); // B0: start with empty childNodes == EmptyNodes
        Element e1 = new Element("span");  // element nodes to be appended
        Element e2 = new Element("p");     // second element
        parent.appendChild(e1);              // B3: ensureChildNodes -> new NodeList, add e1
        parent.appendChild(e2);              // adds second child -> cache not yet built

        // WHEN first call to child(1) builds the cache and returns the second element
        Element firstCall = parent.child(1);
        // Inline comment: childElementsList sees two children, builds a new list of elements => cache created (B5→B6)
        assertSame(e2, firstCall, "First call should return the second child element");

        // Reflectively retrieve the shadowChildrenRef after first call to inspect cache
        Field shadowField = Element.class.getDeclaredField("shadowChildrenRef");
        shadowField.setAccessible(true);
        @SuppressWarnings("unchecked")
        WeakReference<List<Element>> weakRef1 = (WeakReference<List<Element>>) shadowField.get(parent);
        List<Element> cachedList1 = weakRef1.get();

        // WHEN second call to child(1) should reuse the cached list rather than rebuild it
        Element secondCall = parent.child(1);
        // Inline comment: second child(1) call should hit cache branch (shadowChildrenRef!=null, B5 skip B6 rebuild)
        assertSame(e2, secondCall, "Second call should still return the second child element");

        // Reflectively retrieve the weak reference again to ensure it's the same cached list object
        @SuppressWarnings("unchecked")
        WeakReference<List<Element>> weakRef2 = (WeakReference<List<Element>>) shadowField.get(parent);
        List<Element> cachedList2 = weakRef2.get();

        // THEN both calls return the same element and the same cached list instance is used
        assertSame(cachedList1, cachedList2, "Cache list instance should be reused on second call");
        assertEquals(2, cachedList1.size(), "Cached list should contain exactly two children");
    }
}