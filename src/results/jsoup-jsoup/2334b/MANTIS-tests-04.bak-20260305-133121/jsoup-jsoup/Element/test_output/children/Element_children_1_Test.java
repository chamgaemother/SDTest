package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.ref.WeakReference;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Element_children_1_Test {

    @Test
    @DisplayName("children() rebuilds cache when existing WeakReference has been cleared (shadowChildrenRef.get()==null)")
    void test_TC06() throws Exception {
        // GIVEN: parent with one child; first call builds cache (shadowChildrenRef != null, but its referent exists)
        Element parent = new Element("div");
        Element child = parent.appendElement("span");
        Elements firstChildren = parent.children();
        assertEquals(1, firstChildren.size(), "Initial cache should contain the single child");

        // simulate GC clearing of weak reference by setting its referent null
        Field refField = Element.class.getDeclaredField("shadowChildrenRef");
        refField.setAccessible(true);
        // Set a new WeakReference whose get() returns null
        refField.set(parent, new WeakReference<List<Element>>(null));

        // WHEN: children() called again; cache referent is null, so rebuild path should run
        Elements secondChildren = parent.children();

        // THEN: we get a fresh Elements instance containing the single child
        assertEquals(1, secondChildren.size(), "Rebuilt cache should still contain one child");
        assertSame(child, secondChildren.get(0), "The single child in rebuilt cache should be the original child");
    }

    @Test
    @DisplayName("children() rebuilds cache after mutation via prependChild (cache invalidation on nodelistChanged)")
    void test_TC07() {
        // GIVEN: parent with one <li>; children() builds cache
        Element parent = new Element("ul");
        Element e1 = parent.appendElement("li");
        Elements first = parent.children();
        assertEquals(1, first.size(), "Initial cache should have one element");

        // Mutation: prependElement triggers nodelistChanged -> invalidates shadowChildrenRef
        Element e2 = parent.prependElement("li");
        // Prepending puts e2 at index 0; new childNodes size=2

        // WHEN: children() called after mutation; should rebuild to reflect new order
        Elements second = parent.children();

        // THEN: size 2 in new order, and a new Elements instance
        assertEquals(2, second.size(), "After prepend, children() should see two elements");
        assertSame(e2, second.get(0), "First child should be the one prepended");
        assertSame(e1, second.get(1), "Second child should be original child");
        assertNotSame(first, second, "Cache should be rebuilt, returning a new Elements instance");
    }

    @Test
    @DisplayName("children() filters out non-Element nodes and only returns actual Element children")
    void test_TC08() {
        // GIVEN: parent with a TextNode then an Element child
        Element parent = new Element("div");
        parent.appendText("text");  // non-Element node
        Element child = parent.appendElement("span");
        // children() uses childNodes list; only Element instances should be included.

        // WHEN: children() invoked
        Elements result = parent.children();

        // THEN: only the Element node appears
        assertEquals(1, result.size(), "children() should ignore text nodes and only include Elements");
        assertSame(child, result.get(0), "The only child returned should be the <span> element");
    }
}