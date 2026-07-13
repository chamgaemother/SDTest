package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Element_children_2_Test {

    @Test
    @DisplayName("children() uses cached childElementsList when invoked a second time without modifications (cache-hit path)")
    public void test_TC07() throws Exception {
        // GIVEN: a parent with two element children, so childNodeSize>0 and initial cache is empty
        Element parent = new Element("div");
        Element a = new Element("p");
        Element b = new Element("span");
        parent.appendChild(a);
        parent.appendChild(b);
        // WHEN: first invocation to populate the cache (childElementsList called, stashChildren executed)
        Elements first = parent.children();
        // THEN: first should have size 2 and contain our children in order
        assertEquals(2, first.size(), "First children() should list two elements");
        assertSame(a, first.get(0), "First element should be 'a'");
        assertSame(b, first.get(1), "Second element should be 'b'");

        // Access the private childElementsList via reflection to get the cached list instance
        Method childElementsList = Element.class.getDeclaredMethod("childElementsList");
        childElementsList.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Element> cachedFirstList = (List<Element>) childElementsList.invoke(parent);

        // WHEN: second invocation without modifications (cache-hit expected, so same list instance returned)
        Elements second = parent.children();
        assertEquals(2, second.size(), "Second children() should still list two elements");
        assertSame(a, second.get(0), "Second[0] should still be 'a'");
        assertSame(b, second.get(1), "Second[1] should still be 'b'");

        // Verify that the underlying childElementsList is the same instance, indicating a cache hit
        @SuppressWarnings("unchecked")
        List<Element> cachedSecondList = (List<Element>) childElementsList.invoke(parent);
        assertSame(cachedFirstList, cachedSecondList, "childElementsList should be cached (same instance)");
    }

    @Test
    @DisplayName("children() invalidates cache and rebuilds childElementsList when childNodes.modCount changes (cache-miss on modCount mismatch)")
    public void test_TC08() throws Exception {
        // GIVEN: a parent with two element children, initial children() populates cache
        Element parent = new Element("div");
        Element a = new Element("p");
        Element b = new Element("span");
        parent.appendChild(a);
        parent.appendChild(b);
        // populate cache
        Elements first = parent.children();
        assertEquals(2, first.size(), "Initial children() should list two elements");

        // Get the cached childElementsList instance after first call
        Method childElementsList = Element.class.getDeclaredMethod("childElementsList");
        childElementsList.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Element> cachedBefore = (List<Element>) childElementsList.invoke(parent);

        // WHEN: append another child to change modCount, forcing cache invalidation
        Element em = new Element("em");
        parent.appendChild(em); // modCount++

        // THEN: children() should rebuild and include new element
        Elements second = parent.children();
        assertEquals(3, second.size(), "After modification, children() should list three elements");
        assertSame(a, second.get(0), "Second[0] should be original 'a'");
        assertSame(b, second.get(1), "Second[1] should be original 'b'");
        assertEquals("em", second.get(2).tagName(), "Second[2] should be the newly appended 'em'");

        // Verify that the underlying childElementsList instance has changed due to modCount mismatch
        @SuppressWarnings("unchecked")
        List<Element> cachedAfter = (List<Element>) childElementsList.invoke(parent);
        assertNotSame(cachedBefore, cachedAfter, "childElementsList should have been rebuilt (different instance)");
    }
}