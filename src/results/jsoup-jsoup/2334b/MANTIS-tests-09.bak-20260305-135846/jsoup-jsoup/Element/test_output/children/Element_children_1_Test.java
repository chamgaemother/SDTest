package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.Comment;
import org.jsoup.select.Elements;

/**
 * JUnit 5 tests for Element.children() method to cover scenarios TC05 and TC06.
 */
public class Element_children_1_Test {

    @Test
    @DisplayName("TC05: children() on element with non-Element child nodes only returns empty list (childNodeSize()>0, no Element instances)")
    public void test_TC05() {
        // GIVEN an Element with only TextNode and Comment, so childNodeSize()>0 but no child instanceof Element
        Element parent = new Element("div");
        parent.appendChild(new TextNode("text1")); // add a text node
        parent.appendChild(new Comment("cmt1"));   // add a comment node
        // WHEN children() is called
        Elements children = parent.children();
        // THEN expect an empty Elements list (no Element children)
        assertTrue(children.isEmpty(), "Expected no element children when only non-Element nodes are present");
    }

    @Test
    @DisplayName("TC06: children() rebuilds shadow cache after mutation (childNodeSize()>0, shadowChildrenRef expired branch)")
    public void test_TC06() {
        // GIVEN a parent with one <li> child, children() will prime the shadow cache
        Element parent = new Element("ul");
        Element li1 = parent.appendElement("li");
        Elements first = parent.children();
        // confirm initial cache has the single child
        assertEquals(1, first.size(), "First children list should contain exactly one element");
        assertSame(li1, first.get(0), "First children list's element should be the original li1 instance");

        // WHEN we mutate by appending another <li>, which triggers nodelistChanged() and clears cache
        Element li2 = parent.appendElement("li");
        // THEN a new children() call should rebuild list and reflect two children
        Elements second = parent.children();
        assertEquals(2, second.size(), "After mutation, children list should contain two elements");
        // Ensure new cache object returned, not the old one
        assertNotSame(first, second, "Expected a new Elements instance after mutation, not the cached one");
    }
}