package org.jsoup.nodes;

import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JUnit 5 tests for Element.children() method scenarios TC05 and TC06.
 */
public class Element_children_1_Test {

    @Test
    @DisplayName("TC05: children() returns empty when only non-Element nodes present (childNodeSize>0, filter yields none)")
    public void test_TC05() {
        // GIVEN: an Element with only a TextNode child (no Element children to be filtered in)
        Element el = new Element("div");
        el.appendText("text only");
        // childNodeSize > 0 but no Element instances among children -> triggers filter path yielding no elements
        // WHEN
        Elements result = el.children();
        // THEN: expect size 0 as TextNode should be filtered out
        assertEquals(0, result.size(),
            "Expected no element children when only non-Element nodes are present");
    }

    @Test
    @DisplayName("TC06: children() rebuilds list when cache modCount mismatches after mutation (cachedChildren present but stale)")
    public void test_TC06() {
        // GIVEN: a parent with one child element, so first children() builds cache
        Element parent = new Element("ul");
        Element li1 = parent.appendElement("li");
        // first call builds cached children list with modCount for one child
        Elements first = parent.children();
        assertEquals(1, first.size(),
            "Initial children() should see one <li> child");

        // WHEN: append another element child, invalidating cachedChildren modCount
        Element li2 = parent.appendElement("li");
        // second call should detect modCount mismatch and rebuild list
        Elements second = parent.children();

        // THEN: cache was invalidated and new children list has two items
        assertEquals(2, second.size(),
            "After appending second <li>, children() should rebuild and see two <li> children");
    }
}