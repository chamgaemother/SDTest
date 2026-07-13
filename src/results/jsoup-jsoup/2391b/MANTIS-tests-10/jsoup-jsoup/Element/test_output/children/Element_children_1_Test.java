package org.jsoup.nodes;

import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class Element_children_1_Test {

    @Test
    @DisplayName("children() with attributes.userData having jsoup.childElsKey but missing jsoup.childElsMod forces fresh list creation")
    public void test_TC11() throws Exception {
        // GIVEN: parent with one Element child
        org.jsoup.nodes.Element parent = new org.jsoup.nodes.Element("div");
        org.jsoup.nodes.Element child = new org.jsoup.nodes.Element("span");
        parent.appendChild(child);
        // ensure attributes exist and inject only childElsKey, no childElsMod => cache miss
        Map<String, Object> ud = parent.attributes().userData();
        // reflectively get private static childElsKey
        Field keyField = org.jsoup.nodes.Element.class.getDeclaredField("childElsKey");
        keyField.setAccessible(true);
        String childElsKey = (String) keyField.get(null);
        ud.put(childElsKey, new WeakReference<>(List.of(new org.jsoup.nodes.Element("fake"))));

        // WHEN: call children(), branch: cachedChildren() returns null due to missing mod => filterNodes path
        Elements result = parent.children();

        // THEN: fresh list created with actual child
        assertEquals(1, result.size(), "Expected exactly one child element after cache miss");
        assertEquals("span", result.get(0).tagName(), "Expected the actual child tag 'span'");
    }

    @Test
    @DisplayName("children() with attributes.userData having stale WeakReference triggers fresh filterNodes path")
    public void test_TC12() throws Exception {
        // GIVEN: parent with two Element children
        org.jsoup.nodes.Element parent = new org.jsoup.nodes.Element("div");
        org.jsoup.nodes.Element a = new org.jsoup.nodes.Element("a");
        org.jsoup.nodes.Element b = new org.jsoup.nodes.Element("b");
        parent.appendChild(a);
        parent.appendChild(b);
        // inject stale reference (WeakReference.get() -> null) and matching mod count => stale cache
        Map<String, Object> ud = parent.attributes().userData();
        Field keyField = org.jsoup.nodes.Element.class.getDeclaredField("childElsKey");
        keyField.setAccessible(true);
        String childElsKey = (String) keyField.get(null);
        Field modField = org.jsoup.nodes.Element.class.getDeclaredField("childElsMod");
        modField.setAccessible(true);
        String childElsMod = (String) modField.get(null);
        // stale reference: get() returns null
        WeakReference<List<org.jsoup.nodes.Element>> staleRef = new WeakReference<>(null);
        ud.put(childElsKey, staleRef);
        ud.put(childElsMod, parent.childNodeSize()); // simulate matching mod => cachedChildren sees stale ref

        // WHEN: call children(), branch: ref.get()==null => fresh filterNodes path
        Elements result = parent.children();

        // THEN: fresh list created with both children
        assertEquals(2, result.size(), "Expected two children elements after stale cache");
        assertEquals("a", result.get(0).tagName(), "First child should be 'a'");
        assertEquals("b", result.get(1).tagName(), "Second child should be 'b'");
    }
}