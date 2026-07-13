package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.ref.WeakReference;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Element_children_1_Test {

    @Test
    @DisplayName("children() builds list but returns empty when only TextNode children (childNodeSize>0, no Element instances)")
    public void test_TC07() {
        // GIVEN an element with two TextNode children and no Element children
        Element el = new Element("div");
        el.appendText("one"); // adds a TextNode, not an Element
        el.appendText("two"); // adds another TextNode, still no child elements
        // WHEN children() is invoked
        Elements result = el.children();
        // THEN the returned Elements list should be empty (filtering yields no Element instances)
        assertTrue(result.isEmpty(), "Expected no child elements when only TextNode children are present");
    }

    @Test
    @DisplayName("children() returns same cached list after non-structural attribute mutation (cache preserved)")
    public void test_TC08() {
        // GIVEN an element with two Element children, warming the children cache
        Element parent = new Element("div");
        Element child1 = new Element("p");
        Element child2 = new Element("span");
        parent.appendChild(child1); // structural change, builds cache on children()
        parent.appendChild(child2);
        Elements first = parent.children(); // cache stored in shadowChildrenRef
        // WHEN a non-structural attribute is mutated (does not affect childElementsList)
        parent.attr("class", "x"); // only mutates attributes, should not clear child cache
        // AND children() is invoked again
        Elements second = parent.children();
        // THEN the same instance should be returned from cache and size remains 2
        assertSame(first, second, "Expected cached Elements instance after non-structural mutation");
        assertEquals(2, second.size(), "Expected two child elements after attribute mutation");
    }

    @Test
    @DisplayName("children() rebuilds cache when previous cache referent is cleared (WeakReference.get()==null)")
    public void test_TC09() throws Exception {
        // GIVEN an element with three Element children and initialized cache
        Element parent = new Element("div");
        Element a = new Element("a");
        Element b = new Element("b");
        Element c = new Element("c");
        parent.appendChild(a);
        parent.appendChild(b);
        parent.appendChild(c);
        Elements first = parent.children(); // initial cache stored
        // clear the referent of shadowChildrenRef via reflection, simulating GC-cleared referent
        Field refField = Element.class.getDeclaredField("shadowChildrenRef");
        refField.setAccessible(true);
        // set to a new WeakReference with null referent so get() returns null => cache miss
        refField.set(parent, new WeakReference<List<Element>>(null));
        // WHEN children() is invoked after clearing cache referent
        Elements second = parent.children();
        // THEN a new Elements instance should be created and contain three elements
        assertNotSame(first, second, "Expected a new Elements instance after referent cleared");
        assertEquals(3, second.size(), "Expected three child elements after cache rebuild");
    }
}