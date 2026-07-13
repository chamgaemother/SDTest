package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * JUnit 5 tests for the package-private childElementsList() method in org.jsoup.nodes.Element.
 * Reflection is used to access childElementsList and validate caching and cache invalidation behavior.
 */
public class Element_child_1_Test {

    @Test
    @DisplayName("TC07: childElementsList returns cached list on second call when shadowChildrenRef is non-null")
    public void test_TC07() throws Exception {
        // GIVEN: An element with two children; first call to childElementsList populates and caches the child list
        Element el = new Element("div");
        // Append two children; mutation through appendElement uses ensureChildNodes and adds to NodeList, caching shadowChildrenRef on first list call
        el.appendElement("a");
        el.appendElement("b");
        // Reflectively invoke childElementsList first time to initialize cache (shadowChildrenRef non-null afterwards)
        Method childElementsList = Element.class.getDeclaredMethod("childElementsList");
        childElementsList.setAccessible(true);
        // First call builds new list and caches it
        @SuppressWarnings("unchecked")
        List<Element> firstList = (List<Element>) childElementsList.invoke(el);
        // WHEN: Second reflective invocation should return the same cached instance (no rebuild)
        @SuppressWarnings("unchecked")
        List<Element> secondList = (List<Element>) childElementsList.invoke(el);
        // THEN: The two list references must be identical, demonstrating caching
        assertSame(firstList, secondList, "Expected the childElementsList to return the same cached instance on second call");
    }

    @Test
    @DisplayName("TC08: childElementsList rebuilds list after mutation invalidates shadowChildrenRef")
    public void test_TC08() throws Exception {
        // GIVEN: An element with one child; initial call caches list, then appendElement invalidates cache via nodelistChanged
        Element el = new Element("div");
        // Append the first child 'span'
        el.appendElement("span");
        // Reflectively invoke childElementsList to create and cache the initial list
        Method childElementsList = Element.class.getDeclaredMethod("childElementsList");
        childElementsList.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Element> original = (List<Element>) childElementsList.invoke(el);
        // Append another child 'p', which triggers NodeList.onContentsChanged -> nodelistChanged() -> shadowChildrenRef cleared
        el.appendElement("p");
        // WHEN: Reflectively invoke childElementsList again; since cache was invalidated, it must rebuild
        @SuppressWarnings("unchecked")
        List<Element> rebuilt = (List<Element>) childElementsList.invoke(el);
        // THEN: The rebuilt list should include two elements, in correct order, demonstrating cache invalidation and rebuild
        assertEquals(2, rebuilt.size(), "Expected rebuilt childElementsList to contain 2 children after adding a new element");
        assertEquals("p", rebuilt.get(1).tagName(), "Expected the second element in the rebuilt list to have tagName 'p'");
    }
}