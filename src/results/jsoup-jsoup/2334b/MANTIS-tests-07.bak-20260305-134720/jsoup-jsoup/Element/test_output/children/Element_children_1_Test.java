package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class Element_children_1_Test {

    @Test
    @DisplayName("children() on an Element with only non-Element childNodes triggers full build loop and returns empty list")
    public void test_TC06() {
        // GIVEN an Element with a TextNode child (non-Element) to test filtering of non-Element nodes
        Element parent = new Element("div");
        parent.appendText("some text");  // adds a TextNode, not an Element
        // WHEN calling children(), which should iterate childNodes and filter only Element instances
        Elements result = parent.children();
        // THEN the result should be empty because no Element children exist
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("children() when shadowChildrenRef present but referent cleared rebuilds cache and returns correct elements")
    public void test_TC07() throws Exception {
        // GIVEN a parent Element with two Element children to populate the shadowChildrenRef cache
        Element parent = new Element("section");
        Element a = new Element("a");
        Element b = new Element("b");
        parent.appendChild(a);
        parent.appendChild(b);
        parent.children(); // populate cache in shadowChildrenRef
        
        // Reflectively access and clear the WeakReference referent to simulate GC clearing cache
        Field refField = Element.class.getDeclaredField("shadowChildrenRef");
        refField.setAccessible(true);
        @SuppressWarnings("unchecked")
        WeakReference<?> weak = (WeakReference<?>) refField.get(parent);
        weak.clear();  // simulate referent cleared by GC

        // WHEN calling children() again, it should detect empty referent and rebuild the list
        Elements result2 = parent.children();
        // THEN we expect two Element children and both a and b present in the result
        assertEquals(2, result2.size());
        assertTrue(result2.contains(a) && result2.contains(b));
    }
}