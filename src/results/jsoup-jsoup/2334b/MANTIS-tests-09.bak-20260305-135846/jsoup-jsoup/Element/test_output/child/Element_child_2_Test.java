package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
public class Element_child_2_Test {

    @Test
    @DisplayName("TC09: childElementsList() returns cached list on second reflective call when shadowChildrenRef is non-null")
    public void test_TC09() throws Exception {
        // Arrange: Create element with two children so cache is populated on first call
        Element el = new Element("div");
        el.appendElement("a"); // first child, triggers cache build
        el.appendElement("b"); // second child
        // Access the package-private childElementsList method via reflection
        Method m = Element.class.getDeclaredMethod("childElementsList");
        m.setAccessible(true);

        // Act: first call builds and caches the list (shadowChildrenRef set)
        @SuppressWarnings("unchecked")
        List<Element> first = (List<Element>) m.invoke(el);
        // Act: second call should return the same cached instance
        @SuppressWarnings("unchecked")
        List<Element> second = (List<Element>) m.invoke(el);

        // Assert: the cached instance is reused, not rebuilt
        assertSame(first, second, "Expected the same List instance to be returned on second call");
    }

    @Test
    @DisplayName("TC10: childElementsList() rebuilds list after cache invalidation via nodelistChanged()")
    public void test_TC10() throws Exception {
        // Arrange: Create element with one child, build initial cache
        Element el = new Element("div");
        el.appendElement("span"); // triggers initial cache
        Method m = Element.class.getDeclaredMethod("childElementsList");
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Element> original = (List<Element>) m.invoke(el);
        assertEquals(1, original.size(), "Original cache should contain one element");

        // Act: append a new <p> child, which mutates childNodes and invalidates cache via nodelistChanged()
        el.appendElement("p");
        // After mutation, reflective call should rebuild the list
        @SuppressWarnings("unchecked")
        List<Element> rebuilt = (List<Element>) m.invoke(el);

        // Assert: rebuilt list reflects two children in order, and is a new instance
        assertNotSame(original, rebuilt, "Expected a new List instance after cache invalidation");
        assertEquals(2, rebuilt.size(), "Rebuilt list should contain two elements");
        assertEquals("p", rebuilt.get(1).tagName(), "Second element should be the newly appended <p>");
    }
}