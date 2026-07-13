package org.jsoup.nodes;

import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.ref.WeakReference;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class Element_children_1_Test {

    @Test
    @DisplayName("TC06: children() recomputes list when shadowChildrenRef exists but referent has been cleared (shadowChildrenRef.get()==null branch)")
    public void test_TC06() throws Exception {
        // GIVEN: an element with two element children
        Element el = new Element("div");
        Element childA = new Element("a");
        Element childB = new Element("b");
        el.appendChild(childA);
        el.appendChild(childB);
        // Condition B0→B1(false childNodeSize==0): childNodeSize() > 0
        // Prepare for B2(true shadowChildrenRef!=null) & B3(false get()==non-null): set a WeakReference with null referent
        Field refField = Element.class.getDeclaredField("shadowChildrenRef");
        refField.setAccessible(true);
        // Simulate cleared cache: referent is null
        WeakReference<List<Element>> clearedRef = new WeakReference<>(null);
        refField.set(el, clearedRef);

        // WHEN: invoking children(), should enter recompute branch
        Elements result = el.children();

        // THEN: recomputed list contains both children in order
        assertEquals(2, result.size(), "Expected two recomputed child elements");
        assertEquals("a", result.get(0).tagName(), "First child should be 'a'");
        assertEquals("b", result.get(1).tagName(), "Second child should be 'b'");
    }
}