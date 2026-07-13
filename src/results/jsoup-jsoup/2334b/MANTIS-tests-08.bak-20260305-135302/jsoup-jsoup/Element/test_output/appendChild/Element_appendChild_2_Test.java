package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import java.lang.reflect.Field;
import java.util.List;
import java.lang.ref.WeakReference;
import static org.junit.jupiter.api.Assertions.*;
public class Element_appendChild_2_Test {

    @Test
    @DisplayName("appendChild(TextNode) on parent with cached shadowChildrenRef invalidates cache via nodelistChanged")
    public void test_TC07() throws Exception {
        // GIVEN: a parent element with one Element child, and its shadowChildrenRef populated
        Element parent = new Element("div");
        // appendElement returns a new Element child, so childNodes != EmptyNodes and cache will be built
        Element childEl = parent.appendElement("p"); // child is an Element
        // call children() to populate shadowChildrenRef (branch: cache miss, then cache set)
        parent.children();
        // access private shadowChildrenRef via reflection
        Field shadowField = Element.class.getDeclaredField("shadowChildrenRef");
        shadowField.setAccessible(true);
        @SuppressWarnings("unchecked")
        WeakReference<List<Element>> beforeRef = (WeakReference<List<Element>>) shadowField.get(parent);
        assertNotNull(beforeRef, "Precondition: shadowChildrenRef should be non-null after children()");

        // WHEN: appending a TextNode should trigger nodelistChanged and clear the cache
        TextNode tn = new TextNode("foo"); // text node, not an Element, but still triggers cache invalidation
        parent.appendChild(tn); // branch: ensureChildNodes -> add child -> nodelistChanged called

        // THEN: shadowChildrenRef must be nulled out
        @SuppressWarnings("unchecked")
        WeakReference<List<Element>> afterRef = (WeakReference<List<Element>>) shadowField.get(parent);
        assertNull(afterRef, "shadowChildrenRef should be null after appending a child node");
        // children() should still return only the original Element (p)
        assertEquals(1, parent.children().size(), "Only the original Element child should be returned by children()");
    }

    @Test
    @DisplayName("appendChild on parent with >1 existing children assigns correct increasing siblingIndex")
    public void test_TC08() {
        // GIVEN: a parent with two existing TextNode children
        Element parent = new Element("ul");
        TextNode first = new TextNode("one");
        parent.appendChild(first); // first child: siblingIndex = 0
        TextNode second = new TextNode("two");
        parent.appendChild(second); // second child: siblingIndex = 1
        // WHEN: appending a third TextNode should assign siblingIndex = 2 (branch: add to non-empty childNodes)
        TextNode third = new TextNode("three");
        parent.appendChild(third);
        // THEN: childNodeSize should be 3
        assertEquals(3, parent.childNodeSize(), "parent should have three child nodes after three appendChild calls");
        // and the third node's siblingIndex must be 2
        assertEquals(2, third.siblingIndex(), "The third child should have siblingIndex 2 (zero-based increment)");
    }
}