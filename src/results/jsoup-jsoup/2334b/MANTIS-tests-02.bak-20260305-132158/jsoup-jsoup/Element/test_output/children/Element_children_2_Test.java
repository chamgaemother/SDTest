package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.ref.WeakReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class Element_children_2_Test {

    @Test
    @DisplayName("TC08 children() reconstructs shadow cache when existing WeakReference referent has been cleared (shadowChildrenRef!=null and referent==null branch)")
    public void test_TC08() throws Exception {
        // GIVEN: a parent with one child, forcing initial shadow cache creation
        Element parent = new Element("div");
        Element child = parent.appendElement("span");
        // first call populates shadowChildrenRef with a referent list
        Elements firstCall = parent.children();
        assertEquals(1, firstCall.size(), "Initial children() should see one child");
        // clear the referent in shadowChildrenRef to simulate GC-cleared cache
        Field shadowField = Element.class.getDeclaredField("shadowChildrenRef");
        shadowField.setAccessible(true);
        // set to a WeakReference whose referent is null to hit referent==null path
        WeakReference<?> nullRef = new WeakReference<>(null);
        shadowField.set(parent, nullRef);

        // WHEN: children() is called again after cache referent cleared
        Elements secondCall = parent.children();

        // THEN: we expect a fresh Elements list reconstructed containing the same single child
        assertEquals(1, secondCall.size(), "After clearing referent, children() should rebuild and return one child");
        assertEquals(child, secondCall.get(0), "The rebuilt cache should contain the original child element");
    }
}