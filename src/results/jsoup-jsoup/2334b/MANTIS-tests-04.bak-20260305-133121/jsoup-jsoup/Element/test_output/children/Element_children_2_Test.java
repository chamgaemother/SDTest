package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.jsoup.nodes.DataNode; // Importing DataNode class
import org.jsoup.nodes.Element; // Importing Element class
import org.jsoup.nodes.Elements; // Importing Elements class

/**
 * JUnit tests for Element.children() method based on scenarios TC09 and TC10.
 */
public class Element_children_2_Test {
    @Test
    @DisplayName("children() on element with only non-Element child nodes returns empty Elements list")
    public void test_TC09() {
        // GIVEN: a parent with only text and data nodes (no Element children) to trigger the no-element branch (B5)
        Element parent = new Element("div");
        parent.appendText("hello");  // TextNode child ensures childNodes size > 0 but not Element
        parent.appendChild(new DataNode("data")); // DataNode child still not Element
        
        // WHEN: invoke children(), which should filter out non-Element nodes
        Elements result = parent.children();
        
        // THEN: expect an empty Elements list since there are no Element children
        assertNotNull(result, "children() should return a non-null Elements instance");
        assertTrue(result.isEmpty(), "Expected no Element children, but some were found");
    }

    @Test
    @DisplayName("children() second call on multiple-element children returns cached Elements instance")
    public void test_TC10() {
        // GIVEN: parent with two Element children to populate the internal shadowChildrenRef cache (branch B7)
        Element parent = new Element("ul");
        Element li1 = parent.appendElement("li"); // first child, ensures childElementsList built
        Element li2 = parent.appendElement("li"); // second child
        
        // First call builds and caches the childElementsList internally
        Elements first = parent.children();
        assertEquals(2, first.size(), "First children() call should return both appended li elements");
        assertSame(li1, first.get(0), "First element in result should be the first appended child");
        assertSame(li2, first.get(1), "Second element in result should be the second appended child");
        
        // WHEN: second invocation should return the same cached Elements instance according to intended behavior
        Elements second = parent.children();
        
        // THEN: validate that the same Elements instance is returned and contents preserved
        assertSame(first, second, "Expected children() to return the same cached Elements instance on second call");
        assertEquals(2, second.size(), "Cached Elements instance should still contain exactly two elements");
        assertSame(li1, second.get(0), "Cached first element should remain the first appended child");
        assertSame(li2, second.get(1), "Cached second element should remain the second appended child");
    }
}