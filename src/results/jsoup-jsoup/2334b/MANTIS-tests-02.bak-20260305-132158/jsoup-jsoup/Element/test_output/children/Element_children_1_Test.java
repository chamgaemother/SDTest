package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.ref.WeakReference;

import static org.junit.jupiter.api.Assertions.*;
public class Element_children_1_Test {

    @Test
    @DisplayName("TC06 children() on element with only text child nodes returns empty list (childNodeSize>0 but no Element instances)")
    void test_TC06() {
        // GIVEN: a <p> element with only TextNode children -> ensures childNodeSize()>0 but no Element nodes
        Element parent = new Element("p");
        parent.appendText("one");
        parent.appendText("two");
        // WHEN: we call children(), which should filter out non-Element nodes
        Elements result = parent.children();
        // THEN: result.size()==0
        assertEquals(0, result.size(), "children() should return empty when only text nodes are present");
    }

    @Test
    @DisplayName("TC07 children() reuses cached shadowChildrenRef on second call when no mutation occurs (shadowChildrenRef!=null branch)")
    void test_TC07() throws Exception {
        // GIVEN: a <div> element with two Element children a and b -> triggers first build of shadowChildrenRef
        Element parent = new Element("div");
        parent.appendElement("a");
        parent.appendElement("b");
        // first invocation: builds the shadowChildrenRef cache
        Elements first = parent.children();
        assertEquals(2, first.size(), "first.children() should find two Element children");
        // Inspect private field shadowChildrenRef after first call
        Field shadowField = Element.class.getDeclaredField("shadowChildrenRef");
        shadowField.setAccessible(true);
        @SuppressWarnings("unchecked")
        WeakReference<Elements> weakRef1 = (WeakReference<Elements>) shadowField.get(parent);
        assertNotNull(weakRef1, "shadowChildrenRef should be non-null after first children()");
        Elements cacheList1 = weakRef1.get();
        assertNotNull(cacheList1, "cache referent should be non-null after first children()");
        // WHEN: second invocation with no mutation should reuse same cache object
        Elements second = parent.children();
        assertEquals(2, second.size(), "second.children() should still find two Element children");
        // Inspect private field again
        @SuppressWarnings("unchecked")
        WeakReference<Elements> weakRef2 = (WeakReference<Elements>) shadowField.get(parent);
        Elements cacheList2 = weakRef2.get();
        // THEN: the referent list instance should be identical across calls
        assertSame(cacheList1, cacheList2, "children() should reuse the cached shadowChildrenRef referent");
    }
}