package org.jsoup.nodes;

import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.ref.WeakReference;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
public class Element_children_2_Test {

    @Test
    @DisplayName("TC07: children() with shadowChildrenRef non-null but referent GC’d rebuilds list (shadowChildrenRef.get()==null branch)")
    public void test_TC07() throws Exception {
        // GIVEN: a parent element with two child elements, so childElementsList cache is populated
        Element parent = new Element("div");
        Element c1 = parent.appendElement("p");
        Element c2 = parent.appendElement("span");
        // First call to children() primes the shadowChildrenRef cache
        Elements first = parent.children();
        assertEquals(2, first.size()); // ensure cache was built
        // Simulate GC: set shadowChildrenRef to a WeakReference with null referent, so get()==null triggers rebuild
        Field refField = Element.class.getDeclaredField("shadowChildrenRef");
        refField.setAccessible(true);
        // create a WeakReference whose referent is already cleared
        WeakReference<List<Element>> deadRef = new WeakReference<>(null);
        refField.set(parent, deadRef);

        // WHEN: calling children() again should detect null referent and rebuild list
        Elements second = parent.children();

        // THEN: fresh list of two elements is returned in original order
        assertEquals(2, second.size());
        assertSame(c1, second.get(0));
        assertSame(c2, second.get(1));
    }
}