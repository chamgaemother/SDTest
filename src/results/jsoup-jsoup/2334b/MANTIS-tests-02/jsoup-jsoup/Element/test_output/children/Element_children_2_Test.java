package org.jsoup.nodes;

import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.ref.WeakReference;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Element_children_2_Test {

    @Test
    @DisplayName("children() with only non-Element children caches empty list on first call and returns same on second")
    public void test_TC07() throws Exception {
        // GIVEN an element with only TextNode and DataNode children => childNodeSize()>0 but no Element children
        Element parent = new Element("div");
        parent.appendChild(new TextNode("a"));  // non-Element
        parent.appendChild(new DataNode("b"));  // non-Element

        // WHEN first children() is called => B0→B2→B3→B5 path: returns EmptyChildren wrapped
        Elements first = parent.children();
        // WHEN second children() is called => B0→B2→B3→B4→B5 path: should return same cached list instance
        Elements second = parent.children();

        // THEN both should be empty and exactly the same instance (cached)
        assertEquals(0, first.size(), "First call should yield empty Elements");
        assertSame(first, second, "Second call should return the same cached Elements instance");
    }

    @Test
    @DisplayName("children() rebuilds cache when previous WeakReference target has been GC-collected")
    public void test_TC08() throws Exception {
        // GIVEN an element with one Element child => childElementsList builds list [child]
        Element parent = new Element("ul");
        Element child = new Element("li");
        parent.appendChild(child);

        // WHEN first children() is called => caches shadowChildrenRef to list containing child
        Elements first = parent.children();
        assertEquals(1, first.size(), "First call should yield one child element");
        assertSame(child, first.get(0), "First child in first Elements should be the appended element");

        // Simulate garbage-collection of the cached list by clearing the WeakReference in parent via reflection
        Field shadowField = Element.class.getDeclaredField("shadowChildrenRef");
        shadowField.setAccessible(true);
        // Replace with a WeakReference whose referent is already cleared
        shadowField.set(parent, new WeakReference<List<Element>>(null));

        // WHEN second children() is called after simulated GC => rebuild path B2→B3→B5
        Elements second = parent.children();

        // THEN the new Elements should have same content but be a new instance
        assertEquals(1, second.size(), "After cache drop, children() should rebuild list with one element");
        assertSame(child, second.get(0), "Rebuilt Elements should contain the original child element");
        assertNotSame(first, second, "Second Elements instance should not be the same wrapper as the first");
    }
}