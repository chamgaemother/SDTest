package org.jsoup.nodes;

import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for Element.children() covering cache and modCount behavior.
 */
public class Element_children_1_Test {

    @Test
    @DisplayName("children() re-filters when cachedChildren userData exists but modCount mismatches")
    public void test_TC07() {
        // GIVEN: a parent with one child, primed cache via first call
        Element parent = new Element("div");
        Element child = new Element("span");
        parent.appendChild(child);
        // First call populates cache (childNodeSize>0, cachedChildren==null -> stashChildren)
        Elements first = parent.children();
        assertEquals(1, first.size(), "Prime call should see one child");
        // Corrupt the stored modCount to simulate stale cache (mismatch triggers re-filter)
        Map<String, Object> userData = parent.attributes().userData();
        // key for mod count is "jsoup.childElsMod"
        userData.put("jsoup.childElsMod", -1);

        // WHEN: children() called again with stale cache
        Elements result = parent.children();

        // THEN: fresh filtered list returned (size 1, same child instance)
        assertEquals(1, result.size(), "Stale cache should re-filter and return one child");
        assertSame(child, result.get(0), "Returned element must be the original child after re-filtering");
    }

    @Test
    @DisplayName("children() handles multiple element children plus userData present before first call")
    public void test_TC08() {
        // GIVEN: parent with two children and pre-existing unrelated userData
        Element parent = new Element("ul");
        // Pre-populate userData to ensure hasUserData()==true but no cache entries
        parent.attributes().userData().put("fooKey", new Object());
        Element li1 = new Element("li");
        Element li2 = new Element("li");
        parent.appendChild(li1);  // childNodeSize>0
        parent.appendChild(li2);
        // No cache yet: attributes.hasUserData true but no jsoup.childEls key

        // WHEN: children() invoked first time
        Elements result = parent.children();

        // THEN: should filter children in insertion order and stash cache
        assertEquals(2, result.size(), "Should return two children in insertion order");
        assertSame(li1, result.get(0), "First child must be the first appended element");
        assertSame(li2, result.get(1), "Second child must be the second appended element");
    }
}