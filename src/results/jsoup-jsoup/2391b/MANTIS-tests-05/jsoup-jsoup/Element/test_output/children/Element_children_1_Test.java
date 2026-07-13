package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements; // Added import for Elements

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Element_children_1_Test {

    @Test
    @DisplayName("TC05: children() on element with only non-Element child nodes returns empty Elements")
    public void test_TC05() {
        // GIVEN: a parent <div> with exactly one TextNode child (so childNodeSize>0 but no Element children)
        Element parent = new Element("div");
        parent.appendChild(new TextNode("just text"));  // only non-Element child

        // WHEN: calling children()
        Elements result = parent.children();

        // THEN: we expect result.size()==0 since all children are filtered out by children()
        assertEquals(0, result.size(), "Expected no Element children when only TextNode children present");
    }

    @Test
    @DisplayName("TC06: children() cache path: childElementsList returns same cached list instance on second invocation")
    public void test_TC06() throws Exception {
        // GIVEN: a parent <div> with two Element children
        Element parent = new Element("div");
        Element c1 = new Element("p");
        Element c2 = new Element("span");
        parent.appendChild(c1);
        parent.appendChild(c2);
        // The first call will populate the cache (attributes.userData) in childElementsList()

        // USE REFLECTION to call private childElementsList() to inspect caching behavior
        Method childElementsList = Element.class.getDeclaredMethod("childElementsList");
        childElementsList.setAccessible(true);

        // WHEN: first invocation of childElementsList()
        @SuppressWarnings("unchecked")
        List<Element> firstList = (List<Element>) childElementsList.invoke(parent);
        // THEN: it should contain exactly the two appended Elements
        assertEquals(2, firstList.size(), "First childElementsList() should find two children");
        assertTrue(firstList.contains(c1) && firstList.contains(c2), "First list must contain c1 and c2");

        // WHEN: second invocation of childElementsList() (should hit cache and return same instance)
        @SuppressWarnings("unchecked")
        List<Element> secondList = (List<Element>) childElementsList.invoke(parent);

        // THEN: the returned List instance should be the same (cached) instance
        assertSame(firstList, secondList, "Second childElementsList() invocation should return the same cached list instance");
    }
}